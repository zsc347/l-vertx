package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.eventbus.impl.HandlerRegistration;

public class HandlerHolder<T> {

  private final Context context;
  private final HandlerRegistration<T> handler;
  private final boolean isReplyHandler;
  private final boolean isLocalOnly;
  private boolean removed;

  public HandlerHolder(HandlerRegistration<T> handler,
      boolean isReplyHandler,
      boolean isLocalOnly,
      Context context) {
    this.context = context;
    this.handler = handler;
    this.isReplyHandler = isReplyHandler;
    this.isLocalOnly = isLocalOnly;
  }

  public synchronized void setRemoved() {
    if (!removed) {
      removed = true;
    }
  }

  public synchronized boolean isRemoved() {
    return removed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HandlerHolder that = (HandlerHolder) o;
    if (handler != null) {
      return this.handler.equals(that.handler);
    } else {
      return that.handler == null;
    }
  }

  @Override
  public int hashCode() {
    return handler != null ? handler.hashCode() : 0;
  }

  public Context getContext() {
    return context;
  }

  public HandlerRegistration<T> getHandler() {
    return handler;
  }

  public boolean isReplyHandler() {
    return isReplyHandler;
  }

  public boolean isLocalOnly() {
    return isLocalOnly;
  }
}
