package com.scaiz.vertx.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.container.impl.MultiThreadWorkerContext;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import com.scaiz.vertx.container.impl.WorkerContext;
import com.scaiz.vertx.container.impl.WorkerPool;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.impl.NetServerImpl;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.net.transport.Transport;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VertxImpl implements VertxInternal {


  private EventLoopGroup eventLoopGroup;
  private WorkerPool workerPool;
  private WorkerPool internalBlockingPool;

  private Transport transport;

  private EventBus eventBus;

  private final Map<ServerID, NetServerImpl> sharedNetServers = new HashMap<>();


  public VertxImpl() {
    this(new VertxOptions(), null);
  }

  private VertxImpl(VertxOptions options,
      Handler<AsyncResult<Vertx>> resultHandler) {
    Transport nativeTransport = Transport.nativeTransport();
    if (nativeTransport != null && nativeTransport.isAvailable()) {
      transport = nativeTransport;
    } else {
      if (nativeTransport != null && !nativeTransport.isAvailable()) {
        System.err.println("Try to use native port failed");
        nativeTransport.unavailabilityCause().printStackTrace();
      }
      transport = Transport.JDK;
    }

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

    createAndStartEventBus(options, resultHandler);
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
  public ContextImpl getOrCreateContext() {
    ContextImpl context = getContext();
    if (context == null) {
      return createEventLoopContext(this.workerPool,
          Thread.currentThread().getContextClassLoader());
    }
    return context;
  }

  @Override
  public void resolveAddress(String hostname,
      Handler<AsyncResult<InetAddress>> resultHandler) {

  }

  private void createAndStartEventBus(VertxOptions options,
      Handler<AsyncResult<Vertx>> resultHandler) {
    eventBus = new EventBusImpl(this);
    eventBus.start(ar -> {
      if (ar.succeeded()) {
        if (resultHandler != null) {
          resultHandler.handle(Future.succeededFuture(this));
        }
      } else {
        if (resultHandler != null) {
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      }
    });
  }

  @Override
  public EventBus eventBus() {
    if (eventBus == null) {
      // If reading from different thread, possibility that it has been set
      // but not visible, so provide memory barrier here.
      // why not just volatile ?
      synchronized (this) {
        return eventBus;
      }
    }
    return eventBus;
  }

  @Override
  public Handler<Throwable> exceptionHandler() {
    return null;
  }

  @Override
  public NetServer createNetServer(NetServerOptions options) {
    return new NetServerImpl(this, options);
  }

  @Override
  public EventLoopGroup getEventLoopGroup() {
    return eventLoopGroup;
  }

  @Override
  public Map<ServerID, NetServerImpl> sharedNetServers() {
    return sharedNetServers;
  }

  @Override
  public Transport transport() {
    return transport;
  }

  public ContextImpl getContext() {
    Context context = Vertx.currentContext();
    if (context != null && context.owner() == this) {
      return (ContextImpl) context;
    }
    return null;
  }

  public EventLoopContext createEventLoopContext(WorkerPool workerPool,
      ClassLoader tccl) {
    return new EventLoopContext(this,
        tccl, this.internalBlockingPool,
        workerPool != null ? workerPool : this.workerPool);
  }

  public ContextImpl createWorkerContext(boolean multiThreaded,
      WorkerPool workerPool,
      ClassLoader tccl) {
    if (workerPool == null) {
      workerPool = this.workerPool;
    }
    if (multiThreaded) {
      return new MultiThreadWorkerContext(this, tccl, internalBlockingPool,
          workerPool);
    } else {
      return new WorkerContext(this, tccl, internalBlockingPool, workerPool);
    }
  }
}
