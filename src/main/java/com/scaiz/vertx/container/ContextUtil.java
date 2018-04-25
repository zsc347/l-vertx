package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.container.impl.MultiThreadWorkerContext;
import com.scaiz.vertx.container.impl.WorkerContext;

public class ContextUtil {

  public static boolean isOnWorkerThread() {
    if (Thread.currentThread() instanceof VertxThread) {
      return ((VertxThread) Thread.currentThread()).isWorker();
    }
    return false;
  }

  public static boolean isEventLoopContext(Context context) {
    return context instanceof EventLoopContext;
  }

  public static boolean isWorkerContext(Context context) {
    return context instanceof WorkerContext;
  }

  public static boolean isMultiThreadedWorkerContext(Context context) {
    return context instanceof MultiThreadWorkerContext;
  }

  public static Context getCurrentThreadContext() {
    if (Thread.currentThread() instanceof VertxThread) {
      return ((VertxThread) Thread.currentThread()).getContext();
    }
    return null;
  }
}
