package com.scaiz.vertx.support;

public interface ChoosableIterable<T> extends Iterable<T> {

  boolean isEmpty();

  T choose();
}
