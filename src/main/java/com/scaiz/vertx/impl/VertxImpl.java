package com.scaiz.vertx.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import com.scaiz.vertx.container.impl.WorkerPool;
import com.scaiz.vertx.eventbus.EventBus;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VertxImpl implements VertxInternal {


  private EventLoopGroup eventLoopGroup;
  private WorkerPool workerPool;
  private WorkerPool internalBlockingPool;

  public VertxImpl() {
    this(new VertxOptions(), null);
  }

  private VertxImpl(VertxOptions options, Handler<AsyncResult<Vertx>> handler) {
    BlockedThreadChecker checker = new BlockedThreadChecker(
        options.getBlockedThreadCheckInterval());
    ThreadFactory eventLoopThreadFactory = new VertxThreadFactory(
        "vert.x-eventloop-thread-",
        checker, false, options.getMaxEventLoopExecuteTime());
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(
        options.getEventLoopPoolSize(), eventLoopThreadFactory);
    eventLoopGroup.setIoRatio(50);
    this.eventLoopGroup = eventLoopGroup;

    ExecutorService workerExec = Executors
        .newFixedThreadPool(options.getWorkerPoolSize(),
            new VertxThreadFactory("vert.x-worker-thread-", checker, true,
                options.getMaxWorkerExecuteTime()));
    ExecutorService internalBlockingExec = Executors
        .newFixedThreadPool(options.getInternalBlockingPoolSize(),
            new VertxThreadFactory("vert.x-internal-blocking-", checker, true,
                options.getMaxWorkerExecuteTime()));
    workerPool = new WorkerPool(workerExec);
    internalBlockingPool = new WorkerPool(internalBlockingExec);

    if (handler != null) {
      handler.handle(Future.succeededFuture(this));
    }
  }

  @Override
  public void runOnContext(Handler<Void> action) {
    getOrCreateContext().runOnContext(action);
  }

  @Override
  public void cancelTimer(long timeoutID) {

  }

  @Override
  public long setTimer(long delay, Handler<Long> handler) {
    return 0;
  }

  @Override
  public Context getOrCreateContext() {
    Context context = getContext();
    if (context == null) {
      return createEventLoopContext(this.workerPool,
          Thread.currentThread().getContextClassLoader());
    }
    return context;
  }

  @Override
  public EventBus eventBus() {
    return null;
  }

  @Override
  public Handler<Throwable> exceptionHandler() {
    return null;
  }

  @Override
  public EventLoopGroup getEventLoopGroup() {
    return eventLoopGroup;
  }

  public Context getContext() {
    Context context = Vertx.currentContext();
    if (context != null && context.owner() == this) {
      return context;
    }
    return null;
  }

  public EventLoopContext createEventLoopContext(WorkerPool workerPool,
      ClassLoader tccl) {
    return new EventLoopContext(this,
        tccl, this.internalBlockingPool,
        workerPool != null ? workerPool : this.workerPool);
  }
}
