package com.scaiz.vertx.mock;

import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;

public class VertxMock implements Vertx {

  private Context currentContext = new ContextMock();
  private EventBus eventBus;

  @Override
  public void runOnContext(Handler<Void> action) {
    action.handle(null);
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
    return currentContext;
  }

  @Override
  public Context getOrCreateContext() {
    return currentContext;
  }

  @Override
  public EventBus eventBus() {
    if (eventBus == null) {
      eventBus = new EventBusImpl(this);
    }
    return eventBus;
  }
}
