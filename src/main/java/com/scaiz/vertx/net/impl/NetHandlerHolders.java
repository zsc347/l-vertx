package com.scaiz.vertx.net.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class NetHandlerHolders<T> {

  private int pos;
  private final List<NetHandlerHolder<T>> list = new CopyOnWriteArrayList<>();

  NetHandlerHolder<T> chooseHandler() {
    NetHandlerHolder<T> handler = list.get(pos);
    pos++;
    if (pos == list.size()) {
      pos = 0;
    }
    return handler;
  }

  void addHandler(NetHandlerHolder<T> handlerHolder) {
    list.add(handlerHolder);
  }

  boolean removeHandler(NetHandlerHolder<T> handlerHolder) {
    if (list.remove(handlerHolder)) {
      checkPos();
      return true;
    } else {
      return false;
    }
  }

  boolean isEmpty() {
    return list.isEmpty();
  }

  private void checkPos() {
    if (pos == list.size()) {
      pos = 0;
    }
  }
}
