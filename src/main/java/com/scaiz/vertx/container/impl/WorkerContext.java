package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;

public class WorkerContext extends ContextImpl {

  public WorkerContext(VertxInternal vertx,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, tccl, internalWorkerPool, workerPool);
  }

  @Override
  protected void executeAsync(Handler<Void> task) {
    orderedTasks.execute(wrapTask(null, task), workerPool.executor());
  }
}
