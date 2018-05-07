package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.container.impl.ContextImpl;
import java.util.Objects;

public class NetHandlerHolder<T> {

  public final ContextImpl context;
  public final T handler;

  public NetHandlerHolder(ContextImpl context, T handler) {
    this.context = context;
    this.handler = handler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetHandlerHolder that = (NetHandlerHolder) o;
    return Objects.equals(context, that.context) &&
        Objects.equals(handler, that.handler);
  }

  @Override
  public int hashCode() {
    int result = context.hashCode();
    result = 31 * result + handler.hashCode();
    return result;
  }
}
