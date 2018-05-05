package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.ContextUtil;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.HandlerHolder;
import com.scaiz.vertx.eventbus.Handlers;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.SocketAddress;
import com.scaiz.vertx.streams.ReadStream;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Objects;

public class NetServerImpl implements Closeable, NetServer {

  private final VertxInternal vertx;
  private final NetServerOptions options;
  private final Context creatingContext;

  private boolean paused;
  private volatile boolean listening;


  private Handler<NetSocket> handler;
  private Handler<Throwable> exceptionHandler;
  private Context listenContext;
  private Handler<NetSocket> registeredHandler;
  private int actualPort;
  private ServerID id;
  private ChannelGroup serverChannelGroup;
  private EventLoopGroup availableWorkers;


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

  protected void initChannel(ChannelPipeline pipeline) {
    if (options.getIdleTimeout() > 0) {
      pipeline.addLast("idle",
          new IdleStateHandler(0, 0, options.getIdleTimeout()));
    }
  }


  public synchronized void listen(Handler<NetSocket> handler,
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
          protected void initChannel(Channel ch) throws Exception {
            if(isPaused()) {
              ch.close();
              return;
            }
          }
        });
      }
    }

  }

  protected synchronized void pauseAccepting() {
    paused = true;
  }

  protected synchronized void resumeAccepting() {
    paused = false;
  }

  protected synchronized boolean isPaused() {
    return paused;
  }

  protected boolean isListening() {
    return listening;
  }

  @Override
  public ReadStream<NetSocket> connectStream() {
    return null;
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
  public NetServer listen(Handler<AsyncResult<NetServer>> listenHandler) {
    return null;
  }

  @Override
  public NetServer listen() {
    return null;
  }

  @Override
  public NetServer listen(int port, String host,
      Handler<AsyncResult<NetServer>> listenHandler) {
    return null;
  }

  @Override
  public NetServer listen(int port, String host) {
    return null;
  }

  @Override
  public NetServer listen(int port,
      Handler<AsyncResult<NetServer>> listenHandler) {
    return null;
  }

  @Override
  public NetServer listen(int port) {
    return null;
  }

  @Override
  public NetServer listen(SocketAddress localAddress) {
    return null;
  }

  @Override
  public NetServer listen(SocketAddress localAddress,
      Handler<AsyncResult<NetServer>> listenHandler) {
    return null;
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

  @Override
  public void close() {

  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public int actualPort() {
    return 0;
  }
}
