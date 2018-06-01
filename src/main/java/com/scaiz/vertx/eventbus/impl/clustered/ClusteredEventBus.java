package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.eventbus.impl.MessageImpl;
import com.scaiz.vertx.json.JsonObject;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.support.AsyncMultiMap;
import com.scaiz.vertx.support.ChoosableIterable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusteredEventBus extends EventBusImpl {

  private EventBusOptions options;

  private final ClusterManager clusterManager;
  private final HAManager haManager;

  private static final String SUBS_MAP_NAME = "__vertx.subs";
  private static final String SERVER_ID_HA_KEY = "server_id";

  private final ConcurrentMap<ServerID, ConnectionHolder> connections
      = new ConcurrentHashMap<>();
  private final Context sendNoContext;

  private AsyncMultiMap<String, ClusterNodeInfo> subs;
  private NetServer server;
  private ServerID serverID;
  private ClusterNodeInfo nodeInfo;


  public ClusteredEventBus(VertxInternal vertx, VertxOptions vertxOptions,
      ClusterManager clusterManager, HAManager haManager) {
    super(vertx);
    this.options = vertxOptions.getEventBusOptions();
    this.clusterManager = clusterManager;
    this.haManager = haManager;
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
                haManager.addDataToHAInfo(SERVER_ID_HA_KEY,
                    new JsonObject()
                        .put("host", serverID.getHost())
                        .put("port", serverID.getPort()));
                started = true;
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
    String address = sendContext.message().address();
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

    };
  }
}
