package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.ContextImpl;

public class VertxThread extends Thread {

  private Context context;

  public void setContext(ContextImpl context) {
    this.context = context;
  }
}
