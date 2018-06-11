package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.HandlerHolder;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.impl.CodecManager;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.eventbus.impl.MessageImpl;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.parsetools.RecordParser;
import com.scaiz.vertx.support.AsyncMultiMap;
import com.scaiz.vertx.support.ChoosableIterable;
import com.scaiz.vertx.support.MultiMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusteredEventBus extends EventBusImpl {

  private static final Buffer PONG = Buffer.buffer(new byte[]{(byte) 1});

  private EventBusOptions options;

  private final ClusterManager clusterManager;
  private static final String SUBS_MAP_NAME = "__vertx.subs";

  private final ConcurrentMap<ServerID, ConnectionHolder> connections
      = new ConcurrentHashMap<>();
  private final Context sendNoContext;

  private AsyncMultiMap<String, ClusterNodeInfo> subs;
  private NetServer server;
  private ServerID serverID;
  private ClusterNodeInfo nodeInfo;


  public ClusteredEventBus(VertxInternal vertx, VertxOptions vertxOptions,
      ClusterManager clusterManager) {
    super(vertx);
    this.options = vertxOptions.getEventBusOptions();
    this.clusterManager = clusterManager;
    this.sendNoContext = vertx.getOrCreateContext();
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    Handler<Throwable> errorHandler = resultHandler != null
        ? cause -> resultHandler.handle(Future.failedFuture(cause))
        : cause -> {
          System.err.println(cause.getMessage());
          cause.printStackTrace();
        };

    clusterManager.<String, ClusterNodeInfo>getAsyncMultiMap(SUBS_MAP_NAME,
        ar2 -> {
          if (ar2.succeeded()) {
            subs = ar2.result();
            server = vertx.createNetServer(getServerOptions());
            server.connectHandler(getServerHandler());
            server.listen(ar -> {
              if (ar.succeeded()) {
                int serverPort = getClusterPublicPort(options,
                    server.actualPort());
                String serverHost = getClusterPublicHost(options);
                serverID = new ServerID(serverPort, serverHost);
                nodeInfo = new ClusterNodeInfo(clusterManager.getNodeID(),
                    serverID);
                started = true;
                if (resultHandler != null) {
                  resultHandler.handle(Future.succeededFuture());
                }
              } else {
                errorHandler.handle(ar.cause());
              }
            });
          } else {
            errorHandler.handle(ar2.cause());
          }
        });
  }

  @Override
  protected MessageImpl createMessage(boolean send, String address,
      MultiMap headers, Object body, String codecName) {
    Objects.requireNonNull(address, "no null address accepted");
    MessageCodec codec = codecManager.lookupCodec(body, codecName);
    @SuppressWarnings("unchecked")
    ClusteredMessage msg = new ClusteredMessage(serverID, address, null,
        headers, body, codec, send, this);
    return msg;
  }


  @Override
  protected <T> void addRegistration(boolean newAddress, String address,
      boolean replyHandler, boolean localOnly,
      Handler<AsyncResult<Void>> completionHandler) {
    if (newAddress && subs != null && !replyHandler && !localOnly) {
      subs.add(address, nodeInfo, completionHandler);
    } else {
      completionHandler.handle(Future.succeededFuture());
    }
  }

  @Override
  protected <T> void removeRegistration(
      HandlerHolder lastHolder,
      String address,
      Handler<AsyncResult<Void>> completionHandler) {
    if (lastHolder != null && subs != null && !lastHolder.isLocalOnly()) {
      removeSub(address, nodeInfo, completionHandler);
    } else {
      callCompletionHandlerAsync(completionHandler);
    }
  }

  private void removeSub(String subName, ClusterNodeInfo node,
      Handler<AsyncResult<Void>> completionHandler) {
    subs.remove(subName, node, ar -> {
      if (!ar.succeeded()) {
        System.err.println("Failed to remove sub :" + ar.cause().getMessage());
        ar.cause().printStackTrace();
      } else {
        if (ar.result()) {
          if (completionHandler != null) {
            completionHandler.handle(Future.succeededFuture());
          }
        } else {
          if (completionHandler != null) {
            completionHandler.handle(Future.failureFuture("subs not found"));
          }
        }
      }
    });
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    super.close(ar1 -> {
      if (server != null) {
        server.close(ar -> {
          if (ar.failed()) {
            System.err.println(ar.cause().getMessage());
            ar.cause().printStackTrace();
          }
          for (ConnectionHolder holder : connections.values()) {
            holder.close();
          }
          if (completionHandler != null) {
            completionHandler.handle(ar);
          }
        });
      } else {
        if (completionHandler != null) {
          completionHandler.handle(ar1);
        }
      }
    });
  }


  @Override
  protected <T> void sendReply(SendContextImpl<T> sendContext,
      MessageImpl replierMessage) {
    ServerID replyDest = ((ClusteredMessage) replierMessage).getSender();
    MessageImpl message = (MessageImpl) sendContext.message();
    if (!replyDest.equals(serverID)) {
      sendRemote(replyDest, message);
    } else {
      deliverMessageLocally(sendContext);
    }
  }

  @Override
  protected <T> void sendOrPub(SendContextImpl<T> sendContext) {
    Handler<AsyncResult<ChoosableIterable<ClusterNodeInfo>>> resultHandler
        = ar -> {
      if (ar.succeeded()) {
        ChoosableIterable<ClusterNodeInfo> serverIDs = ar.result();
        if (serverIDs != null && !serverIDs.isEmpty()) {
          sendToSubs(serverIDs, sendContext);
        } else {
          deliverMessageLocally(sendContext);
        }
      } else {
        System.err.println(ar.cause().getMessage());
        ar.cause().printStackTrace();
      }
    };

    String address = sendContext.message().address();
    if (Vertx.currentContext() == null) {
      sendNoContext.runOnContext(v -> subs.get(address, resultHandler));
    } else {
      subs.get(address, resultHandler);
    }
  }

  private <T> void sendToSubs(ChoosableIterable<ClusterNodeInfo> subs,
      SendContextImpl<T> sendContext) {
    if (sendContext.message().isSend()) {
      ClusterNodeInfo ci = subs.choose();
      ServerID sid = ci == null ? null : ci.getServerID();
      if (sid != null && !sid.equals(serverID)) {
        sendRemote(sid, sendContext.message());
      } else {
        deliverMessageLocally(sendContext);
      }
    } else {
      boolean local = false;
      for (ClusterNodeInfo ci : subs) {
        if (!ci.getServerID().equals(serverID)) {
          sendRemote(ci.getServerID(), sendContext.message());
        } else {
          local = true;
        }
      }
      if (local) {
        deliverMessageLocally(sendContext);
      }
    }
  }

  private <T> void sendRemote(ServerID theServerId, Message<T> message) {
    ConnectionHolder holder = connections.get(theServerId);
    if (holder == null) {
      holder = new ConnectionHolder(this, theServerId, options);
      ConnectionHolder prevHolder = connections
          .putIfAbsent(theServerId, holder);
      if (prevHolder != null) {
        holder = prevHolder;
      } else {
        holder.connect();
      }
    }
    holder.writeMessage((ClusteredMessage) message);
  }

  @Override
  protected boolean isMessageLocal(MessageImpl msg) {
    ClusteredMessage message = (ClusteredMessage) msg;
    return !message.isFromWire();
  }

  private NetServerOptions getServerOptions() {
    NetServerOptions serverOptions = new NetServerOptions(
        this.options.toJson());
    return serverOptions;
  }

  private String getClusterPublicHost(EventBusOptions options) {
    return options.getClusterPublicHost();
  }

  private int getClusterPublicPort(EventBusOptions options, int actualPort) {
    int clusterPort = options.getClusterPublicPort();
    if (clusterPort == -1) {
      return actualPort;
    }
    return clusterPort;
  }

  private Handler<NetSocket> getServerHandler() {
    return socket -> {
      RecordParser parser = RecordParser.newFixed(4);
      Handler<Buffer> handler = new Handler<Buffer>() {
        int size = -1;

        @Override
        public void handle(Buffer buff) {
          if (size == -1) {
            size = buff.getInt(0);
            parser.fixedSizeMode(size);
          } else {
            ClusteredMessage received = new ClusteredMessage();
            received.readFromWire(buff, codecManager);
            parser.fixedSizeMode(4);
            size = -1;
            if (received.codec() == CodecManager.PING_MESSAGE_CODEC) {
              socket.write(PONG);
            } else {
              deliverMessageLocally(received);
            }
          }
        }
      };
      parser.setOutput(handler);
      socket.handler(parser);
    };
  }

  ConcurrentMap<ServerID, ConnectionHolder> getConnections() {
    return connections;
  }

  VertxInternal vertx() {
    return vertx;
  }

  EventBusOptions options() {
    return options;
  }
}
