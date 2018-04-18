package com.scaiz.vertx.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.eventbus.EventBus;

public class VertxImpl implements Vertx {

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
}
