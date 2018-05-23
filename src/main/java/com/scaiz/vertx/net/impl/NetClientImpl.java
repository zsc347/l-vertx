package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.ContextUtil;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.net.NetClient;
import com.scaiz.vertx.net.NetClientOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.SocketAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetClientImpl implements NetClient {

  private final VertxInternal vertx;
  private final NetClientOptions options;
  private final Closeable closeHook;
  private final ContextImpl creatingContext;
  private final int idleTimeout;
  private boolean closed;
  private final Map<Channel, NetSocketImpl> socketMap =
      new ConcurrentHashMap<>();

  public NetClientImpl(VertxInternal vertx, NetClientOptions options) {
    this(vertx, options, true);
  }

  public NetClientImpl(VertxInternal vertx, NetClientOptions options,
      boolean usingCreatingContext) {
    this.vertx = vertx;
    this.options = new NetClientOptions(options);
    this.closeHook = completionHandler -> {
      NetClientImpl.this.close();
      completionHandler.handle(Future.succeededFuture());
    };
    if (usingCreatingContext) {
      creatingContext = vertx.getContext();
      if (creatingContext != null) {
        if (ContextUtil.isMultiThreadedWorkerContext(creatingContext)) {
          throw new IllegalStateException(
              "can not use NetClient in a multi thread worker verticle");
        }
        creatingContext.addCloseHook(this.closeHook);
      }
    } else {
      creatingContext = null;
    }
    idleTimeout = options.getIdleTimeout();
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("Client is closed");
    }
  }

  protected void initChannel(ChannelPipeline pipeline) {
    if (idleTimeout > 0) {
      pipeline.addLast("idle", new IdleStateHandler(0, 0, idleTimeout));
    }
  }

  private void doConnect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    doConnect(remoteAddress, serverName, connectHandler,
        options.getReconnectAttempts());
  }

  private void doConnect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler, int remainingAttempts) {
    checkClosed();
    if (connectHandler == null) {
      throw new IllegalArgumentException("connect handler must not null");
    }
    ContextImpl context = vertx.getOrCreateContext();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(context.nettyEventLoop());
    bootstrap.channel(vertx.transport().channelType(
        remoteAddress.path() != null));

    applyConnectionOptions(bootstrap);

    ChannelProvider channelProvider;

    if (options.getProxyOption() != null) {
      channelProvider = ProxyChannelProvider.INSTANCE;
    } else {
      channelProvider = ChannelProvider.INSTANCE;
    }

    Handler<Channel> channelInitializer = ch -> {

    };
    Handler<AsyncResult<Channel>> channelHandler = res -> {
      if (res.succeeded()) {
        Channel channel = res.result();
        connected(context, channel, connectHandler);
      } else {
        if (remainingAttempts > 0 || remainingAttempts == -1) {
          context.executeFromIO(() -> System.err.println(
              "Failed to create connection. Will retry in " +
                  options.getReconnectInterval() + " milliseconds"));
        } else {
          failed(context, null, res.cause(), connectHandler);
        }
      }
    };
    channelProvider.connect(vertx, bootstrap, options.getProxyOption(),
        remoteAddress, channelInitializer, channelHandler);
  }


  private void connected(ContextImpl context,
      Channel ch,
      Handler<AsyncResult<NetSocket>> connectHandler) {

    ContextImpl.setCurrentThreadContext(context);
    initChannel(ch.pipeline());

    VertxNetHandler handler = new VertxNetHandler(
        ctx -> new NetSocketImpl(vertx, ctx, context)) {
      @Override
      protected void handleMessage(NetSocketImpl sock, ContextImpl context,
          ChannelHandlerContext chctx, Object message) {
        sock.handleMessageReceived(message);
      }
    };

    handler.setAddHandler(socket -> {
      socketMap.put(ch, socket);
      context.executeFromIO(
          () -> connectHandler.handle(Future.succeededFuture(socket)));
    });

    handler.setRemoveHandler(socketMap::remove);

    ch.pipeline().addLast("handler", handler);
  }


  private void failed(ContextImpl context, Channel ch, Throwable throwable,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    if (ch != null) {
      ch.close();
    }
    context.executeFromIO(
        () -> connectHandler.handle(Future.failedFuture(throwable)));
  }

  private void applyConnectionOptions(Bootstrap bootstrap) {
    vertx.transport().configure(options, bootstrap);
  }

  @Override
  public NetClient connect(int port, String host, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    if (connectHandler == null) {
      throw new IllegalArgumentException("must  set connectHandler");
    }
    doConnect(SocketAddress.inetSocketAddress(port, host), serverName,
        ar -> connectHandler.handle(ar.map(s -> s)));
    return this;
  }

  @Override
  public NetClient connect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    doConnect(remoteAddress, serverName, connectHandler);
    return this;
  }

  @Override
  public void close() {
    if (!closed) {
      for (NetSocketImpl sock : socketMap.values()) {
        sock.close();
      }
      if (creatingContext != null) {
        creatingContext.removeCloseHook(closeHook);
      }
      closed = true;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    // Make sure this get cleaned up if there are no more reference to it
    // so as not to leave connections and resources dangling until the system
    // is shutdown which could make the jvm run out of file handlers
    close();
    super.finalize();
  }
}
