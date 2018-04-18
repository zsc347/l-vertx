package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.container.VertxThread;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class VertxThreadFactory implements ThreadFactory {

  private static final Object DUMMY = new Object();
  private static Map<VertxThread, Object> weakMap = new WeakHashMap<>();
  private final AtomicInteger threadCount = new AtomicInteger(0);

  private final String prefix;
  private final BlockedThreadChecker checker;
  private final boolean isWorker;
  private final long maxExecTime;

  public VertxThreadFactory(String prefix,
      BlockedThreadChecker checker, boolean isWorker, long maxExecTime) {
    this.prefix = prefix;
    this.checker = checker;
    this.isWorker = isWorker;
    this.maxExecTime = maxExecTime;
  }


  private static synchronized void addToMap(VertxThread thread) {
    weakMap.put(thread, DUMMY);
  }

  static synchronized void unsetContext(ContextImpl ctx) {
    for (VertxThread thread : weakMap.keySet()) {
      if (thread.getContext() == ctx) {
        thread.setContext(null);
      }
    }
  }

  @Override
  public Thread newThread(Runnable runnable) {
    VertxThread thread = new VertxThread(runnable,
        prefix + threadCount.getAndIncrement(), isWorker, maxExecTime);
    if (checker != null) {
      checker.registerThread(thread);
    }
    addToMap(thread);
    // prevent jvm exiting util vertx instances closed
    thread.setDaemon(true);
    return thread;
  }
}
