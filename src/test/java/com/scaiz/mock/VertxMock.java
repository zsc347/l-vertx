package com.scaiz.mock;

import com.scaiz.Context;
import com.scaiz.Vertx;
import com.scaiz.async.Handler;
import com.scaiz.eventbus.EventBus;
import com.scaiz.eventbus.impl.EventBusImpl;

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
