package com.scaiz.vertx;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxThread;
import com.scaiz.vertx.eventbus.EventBus;

public interface Vertx {

  static Context currentContext() {
    Thread current = Thread.currentThread();
    if (current instanceof VertxThread) {
      return ((VertxThread) current).getContext();
    }
    return null;
  }

  void runOnContext(Handler<Void> action);

  void cancelTimer(long timeoutID);

  long setTimer(long delay, Handler<Long> handler);

  Context getOrCreateContext();

  EventBus eventBus();

  Handler<Throwable> exceptionHandler();
}
