package com.scaiz.vertx.support;

import java.util.Iterator;

public interface ChoosableIterable<T> extends Iterator<T> {

  boolean isEmpty();

  T choose();
}
