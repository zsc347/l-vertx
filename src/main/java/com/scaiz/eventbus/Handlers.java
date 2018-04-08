package com.scaiz.eventbus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Handlers {

  public final List<HandlerHolder> list = new CopyOnWriteArrayList<>();
  private final AtomicInteger pos = new AtomicInteger(0);

  public HandlerHolder choose() {
    while (true) {
      int size = list.size();
      if (size == 0) {
        return null;
      }
      int p = pos.getAndIncrement();
      if (p >= size - 1) {
        pos.set(0);
      }
      try {
        return list.get(p);
      } catch (IndexOutOfBoundsException e) {
        pos.set(0);
      }
    }
  }
}
