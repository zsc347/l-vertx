package com.scaiz.vertx.eventbus;

public interface SendContext<T> {

  Message<T> message();

  void next();

  boolean isSend();

  Object sentBody();
}
