package com.scaiz.eventbus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Handlers {
  public final List<HandlerHolder> list = new CopyOnWriteArrayList<>();
}
