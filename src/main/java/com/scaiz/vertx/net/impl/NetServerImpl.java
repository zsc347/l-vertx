package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.ContextUtil;
import com.scaiz.vertx.container.VertxEventLoopGroup;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.SocketAddress;
import com.scaiz.vertx.streams.ReadStream;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NetServerImpl implements Closeable, NetServer {

  private final VertxInternal vertx;
  private final NetServerOptions options;
  private final Context creatingContext;
  private final Map<Channel, NetSocketImpl> socketMap = new ConcurrentHashMap<>();

  private boolean paused;
  private volatile boolean listening;
  private NetServerImpl actualServer;

  private ContextImpl listenContext;
  private Handler<NetSocket> registeredHandler;
  private int actualPort;
  private ServerID id;
  private ChannelGroup serverChannelGroup;
  private final VertxEventLoopGroup availableWorkers = new VertxEventLoopGroup();
  private NetHandlerManager<HandlerPair> handlerManager =
      new NetHandlerManager<>(availableWorkers);
  private AsyncResolveConnectHelper bindFuture;

  private Handler<NetSocket> handler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;


  public NetServerImpl(VertxInternal vertx, NetServerOptions options) {
    this.vertx = vertx;
    this.options = new NetServerOptions(options);

    this.creatingContext = Vertx.currentContext();
    if (this.creatingContext != null) {
      if (ContextUtil.isMultiThreadedWorkerContext(this.creatingContext)) {
        throw new IllegalStateException(
            "Can not use NetServer in a multi-thread worker verticle");
      }
      creatingContext.addCloseHook(this);
    }
  }

  private void initChannel(ChannelPipeline pipeline) {
    if (options.getIdleTimeout() > 0) {
      pipeline.addLast("idle",
          new IdleStateHandler(0, 0, options.getIdleTimeout()));
    }
  }

  private synchronized void listen(Handler<NetSocket> handler,
      SocketAddress socketAddress, Handler<AsyncResult<Void>> listenHandler) {
    Objects.requireNonNull(handler, "handler not set");
    if (listening) {
      throw new IllegalStateException("already listening");
    }
    listening = true;
    listenContext = vertx.getOrCreateContext();
    registeredHandler = handler;

    synchronized (vertx.sharedNetServers()) {
      this.actualPort = socketAddress.port();
      String hostOrPath = socketAddress.host() != null
          ? socketAddress.host()
          : socketAddress.path();
      id = new ServerID(actualPort, hostOrPath);
      NetServerImpl shared = vertx.sharedNetServers().get(id);
      if (shared == null || actualPort == 0) {
        serverChannelGroup = new DefaultChannelGroup("vertx-acceptor-channels",
            GlobalEventExecutor.INSTANCE);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(availableWorkers);

        bootstrap.childHandler(new ChannelInitializer<Channel>() {
          @Override
          protected void initChannel(Channel ch) {
            if (isPaused()) {
              ch.close();
              return;
            }
            NetHandlerHolder<HandlerPair> handler = handlerManager
                .chooseHandler(ch.eventLoop());
            if (handler != null) {
              connected(handler, ch);
            }
          }
        });

        applyConnectionOptions(bootstrap);

        handlerManager.addHandler(new HandlerPair(handler, exceptionHandler),
            listenContext);

        try {
          bindFuture = AsyncResolveConnectHelper.doBind(vertx, socketAddress,
              bootstrap);
          bindFuture.addListener(res -> {
            if (res.succeeded()) {
              Channel ch = res.result();
              System.out.println("Net server listening on " + (hostOrPath)
                  + ":" + ch.localAddress());
              // update port to actual port, - wildcard port 0 might have been used
              if (NetServerImpl.this.actualPort != -1) {
                NetServerImpl.this.actualPort = ((InetSocketAddress) ch
                    .localAddress()).getPort();
              }
              NetServerImpl.this.id = new ServerID(
                  NetServerImpl.this.actualPort, id.host);
              serverChannelGroup.add(ch);
              vertx.sharedNetServers().put(id, NetServerImpl.this);
            } else {
              vertx.sharedNetServers().remove(id);
            }
          });
        } catch (Throwable t) {
          // make sure send back the exception back through the handler
          if (listenHandler != null) {
            vertx.runOnContext(
                v -> listenHandler.handle(Future.failedFuture(t)));
          } else {
            t.printStackTrace();
          }
          listening = false;
          return;
        }

        if (actualPort != 0) {
          vertx.sharedNetServers().put(id, this);
        }

        actualServer = this;
      } else {
        actualServer = shared;
        this.actualPort = shared.actualPort();
        actualServer.handlerManager.addHandler(new HandlerPair(
            handler, exceptionHandler), listenContext);
      }

      actualServer.bindFuture.addListener(res -> {
        if (listenHandler != null) {
          AsyncResult<Void> ares;
          if (res.succeeded()) {
            ares = Future.succeededFuture();
          } else {
            listening = false;
            ares = Future.failedFuture(res.cause());
          }
          listenContext.runOnContext(v -> listenHandler.handle(ares));
        } else {
          if (res.failed()) {
            System.err.println("Failed to listen ");
            res.cause().printStackTrace();
            listening = false;
          }
        }
      });
    }
  }

  private NetServer listen(int port, String host,
      Handler<AsyncResult<NetServer>> listenHandler) {
    return listen(SocketAddress.inetSocketAddress(port,host ), listenHandler);
  }

  @Override
  public NetServer listen(Handler<AsyncResult<NetServer>> listenHandler) {
    return listen(options.getPort(), options.getHost(), listenHandler);
  }

  @Override
  public NetServer listen(SocketAddress localAddress,
      Handler<AsyncResult<NetServer>> listenHandler) {
    listen(handler, localAddress, ar -> {
      if (listenHandler != null) {
        listenHandler.handle(ar.map(this));
      }
    });
    return this;
  }

  private void applyConnectionOptions(ServerBootstrap bootstrap) {

  }

  private void connected(NetHandlerHolder<HandlerPair> handlerHolder,
      Channel ch) {
    ContextImpl.setCurrentThreadContext(handlerHolder.context);
    NetServerImpl.this.initChannel(ch.pipeline());

    VertxNetHandler nh = new VertxNetHandler(ctx ->
        new NetSocketImpl(vertx, ctx, handlerHolder.context)) {
      @Override
      protected void handleMessage(NetSocketImpl conn,
          ContextImpl context, ChannelHandlerContext chctx, Object msg) {
        conn.handleMessageReceived(msg);
      }
    };

    nh.setAddHandler(conn -> socketMap.put(ch, conn));
    nh.setRemoveHandler(conn -> socketMap.remove(ch));

    ch.pipeline().addLast("handler", nh);
    NetSocketImpl sock = nh.getConnection();
    handlerHolder.context.executeFromIO(() ->
        handlerHolder.handler.connectionHandler.handle(sock));
  }

  protected synchronized void pauseAccepting() {
    paused = true;
  }

  protected synchronized void resumeAccepting() {
    paused = false;
  }

  public synchronized boolean isPaused() {
    return paused;
  }

  public boolean isListening() {
    return listening;
  }

  @Override
  public NetServer connectHandler(Handler<NetSocket> handler) {
    if (isListening()) {
      throw new IllegalStateException("can not set handler when listening");
    }
    this.handler = handler;
    return this;
  }

  @Override
  public Handler<NetSocket> connectHandler() {
    return handler;
  }


  @Override
  public NetServer exceptionHandler(Handler<Throwable> exceptionHandler) {
    if (isListening()) {
      throw new IllegalStateException(
          "can not set exception handler when server is listening");
    }
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  private void executeCloseDone(ContextImpl closeContext,
      Handler<AsyncResult<Void>> done, Exception e) {
    Future<Void> future = e == null
        ? Future.succeededFuture()
        : Future.failedFuture(e);
    closeContext.runOnContext(v -> done.handle(future));
  }

  private void actualClose(ContextImpl closeContext,
      Handler<AsyncResult<Void>> done) {
    if (id != null) {
      vertx.sharedNetServers().remove(id);
    }

    for (NetSocketImpl sock : socketMap.values()) {
      sock.close();
    }

    ChannelGroupFuture future = serverChannelGroup.close();
    if (done != null) {
      future.addListener(
          cg -> executeCloseDone(closeContext, done, future.cause()));
    }
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (creatingContext != null) {
      creatingContext.removeCloseHook(this);
    }
    Handler<AsyncResult<Void>> done;
    if (endHandler != null) {
      Handler<Void> handler = endHandler;
      endHandler = null;
      done = ar -> {
        if (ar.succeeded()) {
          handler.handle(ar.result());
        }
        if (completionHandler != null) {
          completionHandler.handle(ar);
        }
      };
    } else {
      done = completionHandler;
    }
    ContextImpl context = vertx.getOrCreateContext();

    if (!listening) {
      if (done != null) {
        executeCloseDone(context, done, null);
      }
      return;
    }

    listening = false;

    synchronized (vertx.sharedNetServers()) {
      if (actualServer != null) {
        actualServer.handlerManager.removeHandler(new HandlerPair(
            registeredHandler, exceptionHandler), listenContext);
        if (actualServer.handlerManager.hasHandlers()) {
          // still has handler so not really close it
          if (done != null) {
            executeCloseDone(context, done, null);
          } else {
            actualServer.actualClose(context, done);
          }
        }
      } else {
        context.runOnContext(v -> done.handle(Future.succeededFuture()));
      }
    }
  }

  @Override
  public int actualPort() {
    return actualPort;
  }
}
