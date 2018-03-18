package com.scaiz.async;

public class Executor {

  public static void run(Runnable runnable) {
    runnable.run();
  }
}
