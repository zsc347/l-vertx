package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.net.SocketAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.WriteBufferWaterMark;
import java.net.InetSocketAddress;

public abstract class ConnectionBase {

  protected final VertxInternal vertx;
  protected final ChannelHandlerContext chctx;
  protected final ContextImpl context;

  private boolean read;
  private boolean needsFlush;
  private int writeInProgress;

  private Handler<Void> closeHandler;
  private Handler<Throwable> exceptionHandler;

  protected ConnectionBase(VertxInternal vertx, ChannelHandlerContext chctx,
      ContextImpl context) {
    this.vertx = vertx;
    this.chctx = chctx;
    this.context = context;
  }

  Object encode(Object obj) {
    return obj;
  }

  void checkContext() {
    if (context != vertx.getContext()) {
      throw new IllegalStateException("wrong context");
    }
  }

  public synchronized final void startRead() {
    checkContext();
    read = true;
  }

  protected synchronized final void endReadAndFlush() {
    if (read) {
      read = false;
      if (needsFlush && writeInProgress == 0) {
        needsFlush = false;
        chctx.flush();
      }
    }
  }

  private void write(Object msg, ChannelPromise promise) {
    msg = encode(msg);
    if (read || writeInProgress > 0) {
      needsFlush = true;
      chctx.write(msg, promise);
    } else {
      needsFlush = false;
      chctx.writeAndFlush(msg, promise);
    }
  }

  public synchronized void writeToChannel(Object msg, ChannelPromise promise) {
    if (chctx.executor().inEventLoop() && writeInProgress == 0) {
      write(msg, promise);
    } else {
      queueForWrite(msg, promise);
    }
  }

  private void queueForWrite(Object msg, ChannelPromise promise) {
    writeInProgress++;
    context.runOnContext(v -> {
      synchronized (ConnectionBase.this) {
        writeInProgress--;
        write(msg, promise);
      }
    });
  }

  public void writeToChannel(Object msg) {
    writeToChannel(msg, chctx.voidPromise());
  }

  public boolean isNotWritable() {
    return !chctx.channel().isWritable();
  }

  public void close() {
    endReadAndFlush();
    chctx.channel().close();
  }

  public synchronized ConnectionBase closeHandler(Handler<Void> handler) {
    this.closeHandler = handler;
    return this;
  }

  public synchronized ConnectionBase exceptionHandler(
      Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  protected synchronized Handler<Throwable> exceptionHandler() {
    return this.exceptionHandler;
  }

  public void doPause() {
    chctx.channel().config().setAutoRead(false);
  }

  public void doResume() {
    chctx.channel().config().setAutoRead(true);
  }

  public void doWriteQueueMaxSize(int size) {
    chctx.channel().config()
        .setWriteBufferWaterMark(new WriteBufferWaterMark(size / 2, size));
  }

  public Channel channel() {
    return chctx.channel();
  }

  public ContextImpl getContext() {
    return context;
  }

  protected synchronized void handlerException(Throwable t) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(t);
    } else {
      System.err.println("err " + t);
    }
  }

  protected synchronized void handlerClosed() {
    if (closeHandler != null) {
      vertx.runOnContext(closeHandler);
    }
  }

  protected abstract void handleInterestedOpsChanged();

  protected void addFuture(Handler<AsyncResult<Void>> completionHandler,
      final ChannelFuture future) {
    if (future != null) {
      future.addListener(channelFuture -> context.executeFromIO(() -> {
        if (completionHandler != null) {
          if (channelFuture.isSuccess()) {
            completionHandler.handle(Future.succeededFuture());
          } else {
            completionHandler
                .handle(Future.failedFuture(channelFuture.cause()));
          }
        } else if (!channelFuture.isSuccess()) {
          handlerException(channelFuture.cause());
        }
      }));
    }
  }

  public ChannelPromise channelFuture() {
    return chctx.newPromise();
  }

  public String remoteName() {
    InetSocketAddress addr = (InetSocketAddress) chctx.channel().remoteAddress();
    if (addr == null) return null;
    // Use hostString that does not trigger a DNS resolution
    return addr.getHostString();
  }

  public SocketAddress remoteAddress() {
    InetSocketAddress addr = (InetSocketAddress) chctx.channel().remoteAddress();
    if (addr == null) return null;
    return new SocketAddressImpl(addr);
  }

  public SocketAddress localAddress() {
    InetSocketAddress addr = (InetSocketAddress) chctx.channel().localAddress();
    if (addr == null) return null;
    return new SocketAddressImpl(addr);
  }
}
