package com.scaiz.vertx;

public class VertxOptions {


  public long getBlockedThreadCheckInterval() {
    return 20 * 1000000; // ns
  }

  public long getMaxEventLoopExecuteTime() {
    return 100; // ms
  }

  public int getEventLoopPoolSize() {
    return 10;
  }
}
