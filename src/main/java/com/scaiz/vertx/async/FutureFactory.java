package com.scaiz.vertx.async;

public interface FutureFactory {

  <T> Future<T> future();

  <T> Future<T> succeededFuture();

  <T> Future<T> succeededFuture(T result);

  <T> Future<T> failFuture(Throwable t);
}
