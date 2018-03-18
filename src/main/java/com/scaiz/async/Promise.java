package com.scaiz.async;

import com.scaiz.async.impl.FutureImpl;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Promise<T> {

  private AsyncResult<T> asyncResult;
  private State state;
  private List<CallBackEntry> callBacks;
  private boolean isRunning;

  private enum State {
    PENDING,
    FULFILLED,
    REJECTED
  }

  private static class CallBackEntry<U> {

    private Promise<U> child;
    private Handler<AsyncResult<U>> handler;

    void reset() {
      child = null;
      handler = null;
    }
  }


  Promise(Future<T> future) {
    state = State.PENDING;
    callBacks = new LinkedList<>();

    future.setHandler(ar -> {
      if (ar.succeeded()) {
        this.doResolve(ar.result());
      } else {
        this.doReject(ar.cause());
      }
    });
  }

  private void doReject(Throwable cause) {
    if (!State.PENDING.equals(this.state)) {
      return;
    }
    this.asyncResult = new AsyncResult<T>() {
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
    };
    this.state = State.REJECTED;
    this.scheduleCallbacks();
  }


  private void doResolve(T x) {
    if (!State.PENDING.equals(this.state)) {
      return;
    }
    this.asyncResult = new AsyncResult<T>() {
      @Override
      public T result() {
        return x;
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
    };
    this.state = State.FULFILLED;
    this.scheduleCallbacks();
  }


  public <U> Promise<U> resolve(U u) {
    Promise<U> promise = new Promise<>(null);
    promise.doResolve(u);
    return promise;
  }

  public <U> Promise<U> reject(Throwable err) {
    Promise<U> promise = new Promise<>(null);
    promise.doReject(err);
    return promise;
  }

  public <U> Promise<U> then(Function<AsyncResult<T>, U> mapper) {
    return this.addChildPromise(mapper);
  }

  private <U> Promise<U> addChildPromise(Function<AsyncResult<T>, U> func) {
    CallBackEntry<U> entry = new CallBackEntry();

    Future<U> future = new FutureImpl<>();
    entry.child = new Promise<>(future);

    entry.handler = ar -> {
      if (ar.succeeded()) {
        future.complete(func.apply(this.asyncResult));
      } else {
        future.fail(ar.cause());
      }
    };

    this.addCallbackEntry(entry);
    return entry.child;
  }


  private void addCallbackEntry(CallBackEntry entry) {
    if (!this.hasEntry() && (this.state == State.FULFILLED || this.state == State.REJECTED)) {
      this.scheduleCallbacks();
    }
    this.queueEntry(entry);
  }

  private void queueEntry(CallBackEntry entry) {
    this.callBacks.add(entry);
  }

  private void scheduleCallbacks() {
    if (!this.isRunning) {
      this.isRunning = true;
      Executor.run(this::executeCallbacks);
    }
  }

  private void executeCallbacks() {
    for (CallBackEntry entry : this.callBacks) {
      entry.handler.handle(this.asyncResult);
    }
    this.callBacks = new LinkedList<>();
    this.isRunning = false;
  }

  private boolean hasEntry() {
    return this.callBacks.size() > 0;
  }
}
