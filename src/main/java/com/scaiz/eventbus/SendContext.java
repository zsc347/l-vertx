package com.scaiz.eventbus;

public interface SendContext<T> {

  Message<T> message();

  void next();

  boolean send();

  Object sentBody();
}
