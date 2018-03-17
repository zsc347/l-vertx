package com.scaiz.async.impl;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Future;
import com.scaiz.async.Handler;

class SucceededFuture<T> implements Future<T> {

  private final T result;

  SucceededFuture(T result) {
    this.result = result;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
    handler.handle(this);
    return this;
  }

  @Override
  public void complete(T result) {
    throw new IllegalStateException("Result already succeeded");
  }

  @Override
  public void fail(Throwable cause) {
    throw new IllegalStateException("Result already succeeded");
  }

  @Override
  public void handle(AsyncResult<T> asyncResult) {
    throw new IllegalStateException("Result already succeeded");
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return null;
  }

  @Override
  public boolean succeeded() {
    return true;
  }

  @Override
  public boolean failed() {
    return false;
  }
}
