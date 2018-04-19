package com.scaiz.vertx.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import com.scaiz.vertx.eventbus.EventBus;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ThreadFactory;

public class VertxImpl implements VertxInternal {


  private final ThreadFactory eventLoopThreadFactory;

  private EventLoopGroup eventLoopGroup;


  VertxImpl(VertxOptions options, Handler<AsyncResult<Vertx>> handler) {
    BlockedThreadChecker checker = new BlockedThreadChecker(
        options.getBlockedThreadCheckInterval());
    eventLoopThreadFactory = new VertxThreadFactory("vert.x-eventloop-thread-",
        checker, false, options.getMaxEventLoopExecuteTime());
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(
        options.getEventLoopPoolSize(), eventLoopThreadFactory);
    eventLoopGroup.setIoRatio(50);
    this.eventLoopGroup = eventLoopGroup;
  }

  @Override
  public void runOnContext(Handler<Void> action) {

  }

  @Override
  public void cancelTimer(long timeoutID) {

  }

  @Override
  public long setTimer(long delay, Handler<Long> handler) {
    return 0;
  }

  @Override
  public Context currentContext() {
    return null;
  }

  @Override
  public Context getOrCreateContext() {
    return null;
  }

  @Override
  public EventBus eventBus() {
    return null;
  }

  @Override
  public Handler<Throwable> exceptionHandler() {
    return null;
  }

  @Override
  public EventLoopGroup getEventLoopGroup() {
    return eventLoopGroup;
  }
}
