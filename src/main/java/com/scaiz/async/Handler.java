package com.scaiz.async;

@FunctionalInterface
public interface Handler<E> {

  /**
   * handle event
   */
  void handle(E event);
}
