package com.scaiz.vertx.async.impl;

import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.FutureFactory;

public class FutureFactorImpl implements FutureFactory {

  private static final SucceededFuture EMPTY = new SucceededFuture(null);

  @Override
  public <T> Future<T> future() {
    return new FutureImpl<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Future<T> succeededFuture() {
    return (Future<T>) EMPTY;
  }

  @Override
  public <T> Future<T> succeededFuture(T result) {
    return new SucceededFuture<>(result);
  }

  @Override
  public <T> Future<T> failFuture(Throwable t) {
    return new FailedFuture<>(t);
  }
}
