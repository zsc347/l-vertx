package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.ContextImpl;

public class VertxThread extends Thread {

  private final boolean isWorker;
  private final long maxExecTime;

  private Context context;
  private long execStart;


  public VertxThread(Runnable target, String name, boolean isWorker,
      long maxExecTime) {
    super(target, name);
    this.isWorker = isWorker;
    this.maxExecTime = maxExecTime;
  }

  public void setContext(ContextImpl context) {
    this.context = context;
  }

  public Context getContext() {
    return context;
  }

  public boolean isWorker() {
    return this.isWorker;
  }

  public void executeStart() {
    this.execStart = System.nanoTime();
  }

  public long startTime() {
    return execStart;
  }

  public long getMaxExecTime() {
    return maxExecTime;
  }

  public void executeEnd() {
    execStart = 0;
  }
}
