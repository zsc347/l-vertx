package com.scaiz.vertx.async;

@FunctionalInterface
public interface Handler<E> {

  /**
   * handle event
   */
  void handle(E event);
}
