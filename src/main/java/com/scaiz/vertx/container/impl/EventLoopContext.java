package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

public class EventLoopContext extends ContextImpl {

  public EventLoopContext(VertxInternal vertx,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, tccl, internalWorkerPool, workerPool);
  }

  EventLoopContext(VertxInternal vertx,
      EventLoop eventLoop,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, eventLoop, tccl, internalWorkerPool, workerPool);
  }

  @Override
  protected void executeAsync(Handler<Void> task) {
    this.eventLoop.execute(wrapTask(null, task));
  }
}
