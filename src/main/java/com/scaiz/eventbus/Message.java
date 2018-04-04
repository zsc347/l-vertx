package com.scaiz.eventbus;

import com.scaiz.support.MultiMap;

public interface Message<T> {

  T body();

  MultiMap headers();
}
