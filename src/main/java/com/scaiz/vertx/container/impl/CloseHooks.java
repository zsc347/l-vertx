package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CloseHooks {

  private Set<Closeable> closeHooks;
  private boolean closeHookRun;


  public synchronized void add(Closeable hook) {
    if (closeHooks == null) {
      closeHooks = new HashSet<>();
    }
    closeHooks.add(hook);
  }


  public void remove(Closeable hook) {
    closeHooks.remove(hook);
  }


  void run(Handler<AsyncResult<Void>> completeHandler) {

    Set<Closeable> copy = null;
    synchronized (this) {
      if (closeHookRun) {
        throw new IllegalStateException("Close hooks already run");
      }
      closeHookRun = true;
      if (closeHooks != null && !closeHooks.isEmpty()) {
        copy = new HashSet<>(closeHooks);
      }
    }

    if (copy != null && !copy.isEmpty()) {
      int num = copy.size();
      AtomicInteger count = new AtomicInteger();
      AtomicBoolean failed = new AtomicBoolean();

      // composite future can be used here but not so efficient
      for (Closeable hook : copy) {
        Future<Void> f = Future.future();
        f.setHandler(ar -> {
          if (ar.succeeded()) {
            if (count.incrementAndGet() == num) {
              completeHandler.handle(Future.succeededFuture());
            }
          } else {
            if (failed.compareAndSet(false, true)) {
              completeHandler.handle(Future.failedFuture(ar.cause()));
            }
          }
        });
        try {
          hook.close(f);
        } catch (Throwable t) {
          f.tryFail(t);
        }
      }
    } else {
      completeHandler.handle(Future.succeededFuture());
    }
  }
}
