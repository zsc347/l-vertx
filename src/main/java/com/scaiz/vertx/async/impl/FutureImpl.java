package com.scaiz.vertx.async.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;

public class FutureImpl<T> implements Future<T> {

  private boolean failed;
  private boolean succeeded;
  private Handler<AsyncResult<T>> handler;
  private T result;
  private Throwable throwable;

  public FutureImpl() {
  }

  @Override
  public boolean isComplete() {
    return failed || succeeded;
  }

  @Override
  public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
    boolean callHandler;
    synchronized (this) {
      this.handler = handler;
      callHandler = isComplete();
    }
    if (callHandler) {
      handler.handle(this);
    }
    return this;
  }

  @Override
  public void complete(T result) {
    if (!tryComplete(result)) {
      throw new IllegalStateException(
          "Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }

  private boolean tryComplete(T result) {
    Handler<AsyncResult<T>> h;
    synchronized (this) {
      if (isComplete()) {
        return false;
      }
      this.result = result;
      this.succeeded = true;
      h = this.handler;
    }
    if (h != null) {
      h.handle(this);
    }
    return true;
  }

  @Override
  public void fail(Throwable cause) {
    if (!this.tryFail(cause)) {
      throw new IllegalStateException(
          "Result is already complete: " + (succeeded ? "succeeded" : "failed"));
    }
  }

  private boolean tryFail(Throwable cause) {
    Handler<AsyncResult<T>> h;
    synchronized (this) {
      if (isComplete()) {
        return false;
      }
      this.throwable = cause;
      this.failed = true;
      h = this.handler;
    }
    if (h != null) {
      h.handle(this);
    }
    return true;
  }

  @Override
  public void handle(AsyncResult<T> asyncResult) {
    if (asyncResult.succeeded()) {
      complete(asyncResult.result());
    } else {
      fail(asyncResult.cause());
    }
  }

  @Override
  public T result() {
    return result;
  }

  @Override
  public Throwable cause() {
    return throwable;
  }

  @Override
  public boolean succeeded() {
    return succeeded;
  }

  @Override
  public boolean failed() {
    return failed;
  }
}
