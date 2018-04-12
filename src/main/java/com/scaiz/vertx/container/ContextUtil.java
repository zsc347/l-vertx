package com.scaiz.vertx.container;

public class ContextUtil {

  boolean isEventLoopContext(Context context) {
    return false;
  }

  boolean isWorkerContext(Context context) {
    return false;
  }

  boolean isMultiThreadedWorkerContext(Context context) {
    return false;
  }

}
