package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.EventLoopContext;

public class ContextUtil {

  public static boolean isEventLoopContext(Context context) {
    return context instanceof EventLoopContext;
  }

  public static boolean isWorkerContext(Context context) {
    return false;
  }

  public static boolean isMultiThreadedWorkerContext(Context context) {
    return false;
  }
}
