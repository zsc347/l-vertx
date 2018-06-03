package com.scaiz.vertx.async;

import com.scaiz.vertx.async.impl.NoStackTraceThrowable;

public interface FutureFactory {

  <T> Future<T> future();

  <T> Future<T> succeededFuture();

  <T> Future<T> succeededFuture(T result);

  <T> Future<T> failFuture(Throwable t);

  default <T> Future<T> failureFuture(String message) {
    return failFuture(new NoStackTraceThrowable(message));
  }
}
