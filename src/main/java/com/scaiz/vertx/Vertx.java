package com.scaiz.vertx;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.eventbus.EventBus;

public interface Vertx {

  void runOnContext(Handler<Void> action);

  void cancelTimer(long timeoutID);

  long setTimer(long delay, Handler<Long> handler);

  Context currentContext();

  Context getOrCreateContext();

  EventBus eventBus();

  Handler<Throwable> exceptionHandler();
}
