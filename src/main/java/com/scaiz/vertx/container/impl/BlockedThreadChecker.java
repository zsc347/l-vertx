package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.VertxException;
import com.scaiz.vertx.container.VertxThread;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

public class BlockedThreadChecker {

  private final Set<VertxThread> threads = Collections
      .newSetFromMap(new WeakHashMap<>());
  private final Timer timer;

  public BlockedThreadChecker(long interval) {
    this.timer = new Timer("vertx-blocked-thread-checker", true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        synchronized (BlockedThreadChecker.this) {
          long now = System.nanoTime();
          for (VertxThread thread : threads) {
            long execStart = thread.startTime();
            long dur = now - execStart;
            long timeLimit = thread.getMaxExecTime();
            if (execStart != 0 && dur > timeLimit) {
              String msg = "Thread " + thread + " has been blocked for " + (dur
                  / 1000000) + " ms, time limit is " + (timeLimit / 1000000);
              VertxException stackTrace = new VertxException(msg);
              stackTrace.setStackTrace(thread.getStackTrace());
              // TODO add log implementation to print stacktrace
              System.err.println(msg);
              System.err.println(stackTrace.toString());
            }
          }
        }
      }
    }, interval, interval);
  }

  public void registerThread(VertxThread thread) {
    threads.add(thread);
  }

  public void close() {
    timer.cancel();
  }
}
