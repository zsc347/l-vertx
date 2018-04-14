package com.scaiz.vertx.async.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;

public class CompositeFuture implements Future<CompositeFuture> {

  private final Future[] results;
  private int count;
  private boolean completed;
  private Throwable cause;
  private Handler<AsyncResult<CompositeFuture>> handler;

  private static final Handler<AsyncResult<CompositeFuture>> NO_HANDLER = c -> {
  };

  private CompositeFuture(Future[] results) {
    this.results = results;
  }

  public static CompositeFuture any(Future<?>... results) {
    CompositeFuture composite = new CompositeFuture(results);
    for (Future<?> result : results) {
      result.setHandler(ar -> {
        Handler<AsyncResult<CompositeFuture>> handler = null;
        if (ar.succeeded()) {
          synchronized (composite) {
            if (!composite.isComplete()) {
              composite.setCompleted(null);
            }
          }
        } else {
          synchronized (composite) {
            composite.count++;
            if (!composite.isComplete() && composite.count == results.length) {
              handler = composite.setCompleted(ar.cause());
            }
          }
        }

        if (handler != null) {
          handler.handle(composite);
        }
      });
    }
    if (results.length == 0) {
      composite.complete(null);
    }
    return composite;
  }

  public static CompositeFuture all(Future<?>... results) {
    CompositeFuture composite = new CompositeFuture(results);
    for (Future<?> result : results) {
      result.setHandler(ar -> {
        Handler<AsyncResult<CompositeFuture>> handler = null;
        if (ar.succeeded()) {
          synchronized (composite) {
            composite.count++;
            if (!composite.isComplete() && composite.count != results.length) {
              handler = composite.setCompleted(null);
            }
          }
        } else {
          synchronized (composite) {
            if (!composite.isComplete()) {
              handler = composite.setCompleted(ar.cause());
            }
          }
        }

        if (handler != null) {
          handler.handle(composite);
        }
      });
    }
    if (results.length == 0) {
      composite.complete(null);
    }
    return composite;
  }


  public static CompositeFuture join(Future<?>... results) {
    CompositeFuture composite = new CompositeFuture(results);
    for (Future<?> result : results) {
      result.setHandler(ar -> {
        Handler<AsyncResult<CompositeFuture>> handler = null;
        synchronized (composite) {
          composite.count++;
          if (!composite.isComplete() && composite.count == results.length) {
            for (Future rs : composite.results) {
              if (rs.failed()) {
                handler = composite.setCompleted(rs.cause());
              }
            }
          }
        }
        if (handler != null) {
          handler.handle(composite);
        }
      });
    }

    if (results.length == 0) {
      composite.complete(null);
    }
    return composite;
  }

  @Override
  public boolean isComplete() {
    return completed;
  }

  @Override
  public Future<CompositeFuture> setHandler(
      Handler<AsyncResult<CompositeFuture>> handler) {
    boolean call;
    synchronized (this) {
      this.handler = handler;
      call = completed;
    }
    if (call) {
      handler.handle(this);
    }
    return this;
  }

  @Override
  public void complete(CompositeFuture result) {
    Handler<AsyncResult<CompositeFuture>> handler = setCompleted(null);
    if (handler == null) {
      throw new IllegalStateException(
          "Future already completed: " + (this.cause != null ? "succeeded"
              : "failed"));
    }
  }

  private Handler<AsyncResult<CompositeFuture>> setCompleted(Throwable cause) {
    synchronized (this) {
      if (completed) {
        return null;
      }
      this.completed = true;
      this.cause = cause;
      return handler != null ? handler : NO_HANDLER;
    }
  }

  @Override
  public void fail(Throwable cause) {
    if (!tryFail(cause())) {
      throw new IllegalStateException(
          "Result is already complete: " + (this.cause == null
              ? "succeeded" : "failed"));
    }
  }

  @Override
  public boolean tryFail(Throwable cause) {
    Handler<AsyncResult<CompositeFuture>> handler = setCompleted(cause);
    if (handler != null) {
      handler.handle(this);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void handle(AsyncResult<CompositeFuture> asyncResult) {
    if (asyncResult.succeeded()) {
      complete(this);
    } else {
      fail(asyncResult.cause());
    }
  }

  @Override
  public CompositeFuture result() {
    return completed ? this : null;
  }

  @Override
  public Throwable cause() {
    return completed && cause != null ? cause : null;
  }

  @Override
  public boolean succeeded() {
    return completed && cause == null;
  }

  @Override
  public boolean failed() {
    return completed && cause != null;
  }
}
