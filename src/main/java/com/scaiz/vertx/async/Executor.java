package com.scaiz.vertx.async;

public interface Executor {

  public static void run(Runnable runnable) {
    runnable.run();
  }
}
