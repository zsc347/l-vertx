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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class NetClientImpl implements NetClient {

  private final VertxInternal vertx;
  private final NetClientOptions options;
  private final Closeable closeHook;
  private final ContextImpl creatingContext;
  private final int idleTimeout;
  private boolean closed;

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




  }

  private void applyConnectionOptions(Bootstrap bootstrap) {
    vertx.transport().configure(options, bootstrap);
  }

  @Override
  public NetClient connect(int port, String host, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler) {

    return this;
  }

  @Override
  public NetClient connect(SocketAddress remoteAddress,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    return null;
  }

  @Override
  public NetClient connect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    return null;
  }

  @Override
  public void close() {

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
