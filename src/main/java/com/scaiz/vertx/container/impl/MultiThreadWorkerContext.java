package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;

public class MultiThreadWorkerContext extends WorkerContext {

  public MultiThreadWorkerContext(VertxInternal vertx,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, tccl, internalWorkerPool, workerPool);
  }

  @Override
  public void executeAsync(Handler<Void> task) {
    workerPool.executor().execute(wrapTask(null, task));
  }
}
