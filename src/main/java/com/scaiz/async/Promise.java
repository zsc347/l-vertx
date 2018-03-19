package com.scaiz.async;

import com.scaiz.async.impl.FutureImpl;
import java.util.function.Function;
import jdk.nashorn.internal.codegen.CompilerConstants.Call;

public class Promise<T> {

  private AsyncResult<T> asyncResult;
  private State state;
  private CallBackEntry entryTail;
  private boolean isRunning;

  private enum State {
    PENDING,
    FULFILLED,
    REJECTED
  }

  private static class CallBackEntry<U> {

    private Promise<U> child;
    private Handler<AsyncResult<U>> handler;
    private CallBackEntry<U> next;
  }

  private Promise() {
    state = State.PENDING;
  }

  Promise(Future<T> future) {
    state = State.PENDING;

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
    schedule();
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
    schedule();
  }


  public static <U> Promise<U> resolve(U u) {
    Promise<U> promise = new Promise<>();
    promise.doResolve(u);
    return promise;
  }

  public static <U> Promise<U> reject(Throwable err) {
    Promise<U> promise = new Promise<>();
    promise.doReject(err);
    return promise;
  }

  public <U> Promise<U> then(Function<AsyncResult<T>, U> func) {
    CallBackEntry<U> entry = new CallBackEntry<>();

    Future<U> future = new FutureImpl<>();
    entry.child = new Promise<>(future);
    entry.handler = ar -> {
      if (ar.succeeded()) {
        try {
          future.complete(func.apply(this.asyncResult));
        } catch (Throwable err) {
          future.fail(err);
        }
      } else {
        future.fail(ar.cause());
      }
    };

    addCallbackEntry(entry);
    return entry.child;
  }

  public <U> Promise<U> thenCatch(Function<Throwable, U> func) {
    CallBackEntry<U> entry = new CallBackEntry<>();
    Future<U> future = new FutureImpl<>();
    entry.child = new Promise<>(future);

    entry.handler = ar -> {
      if (ar.failed()) {
        try {
          future.complete(func.apply(ar.cause()));
        } catch (Throwable err) {
          future.fail(err);
        }
      } else {
        future.complete(ar.result());
      }
    };
    addCallbackEntry(entry);
    return entry.child;
  }

  private synchronized <U> void addCallbackEntry(CallBackEntry<U> entry) {
    /* if schedule is sync, then this.hasEntry() must be false
     * schedule will execute nothing because no callback
     *
     * if schedule is async, this check ensure schedule will only
     * be triggered once.
     */
    if (!this.hasEntry() && !State.PENDING.equals(this.state)) {
      schedule();
    }

    queueEntry(entry);

    /* if schedule is sync, then isRunning must be false,
     * entry handler will be run immediately
     *
     * if schedule is async, then isRunning must be true,
     * following won't run again.
     */
    if (!this.isRunning && !State.PENDING.equals(this.state)) {
      schedule();
    }
  }

  private void queueEntry(CallBackEntry entry) {
    if (this.entryTail == null) {
      this.entryTail = entry;
    }
    entry.next = this.entryTail.next;
    this.entryTail.next = entry;
    this.entryTail = entry;
  }

  private void schedule() {
    if (!this.isRunning) {
      this.isRunning = true;
      Executor.run(this::executeCallbacks);
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized void executeCallbacks() {
    if (this.entryTail != null) {
      CallBackEntry head = this.entryTail.next;
      while (head != this.entryTail) {
        head.handler.handle(this.asyncResult);
        head = head.next;
      }
      head.handler.handle(this.asyncResult);
    }
    this.entryTail = null;
    this.isRunning = false;
  }

  private boolean hasEntry() {
    return this.entryTail != null;
  }
}
