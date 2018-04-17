package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

public class EventLoopContext extends ContextImpl {

  private EventLoop eventLoop;

  EventLoopContext(VertxInternal vertx,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, tccl, internalWorkerPool, workerPool);
    this.eventLoop = nettyEventLoop(vertx);
  }

  EventLoopContext(VertxInternal vertx,
      EventLoop eventLoop,
      ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    super(vertx, tccl, internalWorkerPool, workerPool);
    this.eventLoop = eventLoop;
  }

  private static EventLoop nettyEventLoop(VertxInternal vertx) {
    EventLoopGroup group = vertx.getEventLoopGroup();
    if (group != null) {
      return group.next();
    } else {
      return null;
    }
  }

  @Override
  protected void executeAsync(Handler<Void> task) {
    this.eventLoop.execute(wrapTask(null, task));
  }
}
