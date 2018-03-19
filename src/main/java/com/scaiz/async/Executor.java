package com.scaiz.async;

public interface Executor {

  public static void run(Runnable runnable) {
    runnable.run();
  }
}
