package com.scaiz;

import com.scaiz.async.Handler;
import com.scaiz.eventbus.EventBus;

public interface Vertx {

  void runOnContext(Handler<Void> action);

  EventBus eventBus();
}
