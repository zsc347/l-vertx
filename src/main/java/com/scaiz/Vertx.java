package com.scaiz;

import com.scaiz.async.Handler;
import com.scaiz.eventbus.EventBus;

public interface Vertx {

  void runOnContext(Handler<Void> action);

  void cancelTimer(long timeoutID);
  
  long setTimer(long delay, Handler<Long> handler);

  Context currentContext();

  Context getOrCreateContext();

  EventBus eventBus();
}
