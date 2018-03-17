package com.scaiz.async.impl;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Future;
import com.scaiz.async.Handler;
import java.util.Optional;

public class FailedFuture<T> implements Future<T> {

  private final Throwable cause;

  public FailedFuture(Throwable cause) {
    this.cause = Optional.ofNullable(cause).orElse(new NoStackTraceThrowable(null));
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
    throw new IllegalStateException("Result already complete: failed");
  }

  @Override
  public void fail(Throwable cause) {
    throw new IllegalStateException("Result already complete: failed");
  }

  @Override
  public void handle(AsyncResult<T> asyncResult) {
    throw new IllegalStateException("Result already complete: failed");
  }

  @Override
  public T result() {
    return null;
  }

  @Override
  public Throwable cause() {
    return cause;
  }

  @Override
  public boolean succeeded() {
    return false;
  }

  @Override
  public boolean failed() {
    return true;
  }
}
