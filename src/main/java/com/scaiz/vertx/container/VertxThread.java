package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.ContextImpl;

public class VertxThread extends Thread {

  private Context context;

  public VertxThread(Runnable runnable, String prefix, boolean isWorker,
      long maxExecTime) {

  }

  public void setContext(ContextImpl context) {
    this.context = context;
  }

  public Context getContext() {
    return context;
  }

  public boolean isWorker() {
    return false;
  }

  public void executeStart() {
  }

  public void executeEnd() {
  }
}
