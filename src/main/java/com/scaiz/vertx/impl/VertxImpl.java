package com.scaiz.vertx.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.CloseHooks;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.container.impl.MultiThreadWorkerContext;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import com.scaiz.vertx.container.impl.WorkerContext;
import com.scaiz.vertx.container.impl.WorkerPool;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.eventbus.impl.clustered.ClusterManager;
import com.scaiz.vertx.eventbus.impl.clustered.ClusteredEventBus;
import com.scaiz.vertx.net.NetClient;
import com.scaiz.vertx.net.NetClientOptions;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;
import com.scaiz.vertx.net.impl.NetClientImpl;
import com.scaiz.vertx.net.impl.NetServerImpl;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.net.resolver.AddressResolver;
import com.scaiz.vertx.net.transport.Transport;
import com.scaiz.vertx.spi.ServiceHelper;
import io.netty.channel.EventLoopGroup;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VertxImpl implements VertxInternal {


  private EventLoopGroup eventLoopGroup;
  private WorkerPool workerPool;
  private WorkerPool internalBlockingPool;

  private Transport transport;

  private EventBus eventBus;

  private final Map<ServerID, NetServerImpl> sharedNetServers = new HashMap<>();

  private final AddressResolver addressResolver;
  private final ClusterManager clusterManager;

  private boolean closed;

  private final CloseHooks closeHooks;

  private BlockedThreadChecker checker;
  private ThreadFactory eventLoopThreadFactory;

  public VertxImpl() {
    this(new VertxOptions(), null);
  }

  public VertxImpl(VertxOptions options,
      Handler<AsyncResult<Vertx>> resultHandler) {
    Transport nativeTransport = Transport.nativeTransport();
    if (nativeTransport != null && nativeTransport.isAvailable()) {
      transport = nativeTransport;
    } else {
      if (nativeTransport != null && !nativeTransport.isAvailable()) {
        System.err.println("Try to use native port failed");
        // nativeTransport.unavailabilityCause().printStackTrace();
      }
      transport = Transport.JDK;
    }

    closeHooks = new CloseHooks();

    checker = new BlockedThreadChecker(
        options.getBlockedThreadCheckInterval());
    eventLoopThreadFactory = new VertxThreadFactory(
        "vert.x-eventloop-thread-",
        checker, false, options.getMaxEventLoopExecuteTime());
    eventLoopGroup = transport.eventLoopGroup(options.getEventLoopPoolSize(),
        eventLoopThreadFactory, 50);

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

    this.addressResolver = new AddressResolver(this,
        options.getAddressResolverOptions());

    if (options.isClustered()) {
      this.clusterManager = getClusterManager(options);
      this.clusterManager.setVertx(this);
      this.clusterManager.join(ar -> {
        if (ar.failed()) {
          System.err.println(
              "Failed to join cluster " + ar.cause().getMessage());
          ar.cause().printStackTrace();
        } else {
          synchronized (VertxImpl.this) {
            createAndStartEventBus(options, resultHandler);
          }
        }
      });
    } else {
      this.clusterManager = null;
      createAndStartEventBus(options, resultHandler);
    }
  }

  private ClusterManager getClusterManager(VertxOptions options) {
    if (options.isClustered()) {
      if (options.getClusteredManger() != null) {
        return options.getClusteredManger();
      } else {
        ClusterManager mgr;
        String clusterManagerClassName = System
            .getProperty("vertx.cluster.managerClass");
        if (clusterManagerClassName != null) {
          // We allow specify a sys prop for the cluster manager factory which overrides ServiceLoader
          try {
            Class<?> clazz = Class.forName(clusterManagerClassName);
            mgr = (ClusterManager) clazz.newInstance();
          } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to instantiate " + clusterManagerClassName, e);
          }
        } else {
          mgr = ServiceHelper.loadFactoryOrNull(ClusterManager.class);
          if (mgr == null) {
            throw new IllegalStateException(
                "No ClusterManagerFactory instances found on classpath");
          }
        }
        return mgr;
      }
    } else {
      return null;
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
    this.addressResolver.resolveHostName(hostname, resultHandler);
  }

  @Override
  public AddressResolverGroup<?> nettyAddressResolverGroup() {
    return addressResolver.nettyAddressResolverGroup();
  }

  private void createAndStartEventBus(VertxOptions options,
      Handler<AsyncResult<Vertx>> resultHandler) {
    if (options.isClustered()) {
      eventBus = new ClusteredEventBus(this, options, clusterManager);
    } else {
      eventBus = new EventBusImpl(this);
    }

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
  public NetClient createNetClient(NetClientOptions options) {
    return new NetClientImpl(this, options);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed || eventBus == null) {
      if (completionHandler != null) {
        completionHandler.handle(Future.succeededFuture());
      }
      return;
    }
    closed = true;
    closeHooks.run(ar -> {
      addressResolver.close(ar2 -> {
        eventBus.close(ar3 -> {
          closeClusterManager(ar4 -> {
            Set<NetServer> netServers = new HashSet<>(
                sharedNetServers.values());
            netServers.clear();

            int serverCount = netServers.size();
            AtomicInteger serverCloseCount = new AtomicInteger();

            Handler<AsyncResult<Void>> serverCloseHandler = res -> {
              if (res.failed()) {
                System.err.println("Failed in shutting down server: "
                    + res.cause().getMessage());
                res.cause().printStackTrace();
              }
              if (serverCloseCount.incrementAndGet() == serverCount) {
                shutdown(completionHandler);
              }
            };
            for (NetServer server : netServers) {
              server.close(serverCloseHandler);
            }
            if (serverCount == 0) {
              shutdown(completionHandler);
            }
          });
        });
      });
    });
  }

  // deleteCacheDirAndShutDown
  @SuppressWarnings("unchecked")
  private void shutdown(Handler<AsyncResult<Void>> completionHandler) {
    workerPool.close();
    internalBlockingPool.close();

    eventLoopGroup.shutdownGracefully(0, 10, TimeUnit.SECONDS)
        .addListener((GenericFutureListener) future -> {
              checker.close();
              if (completionHandler != null) {
                eventLoopThreadFactory.newThread(
                    () -> completionHandler.handle(Future.succeededFuture()))
                    .start();
              }
            }
        );
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

  private void closeClusterManager(
      Handler<AsyncResult<Void>> completionHandler) {
    if (clusterManager != null) {
      clusterManager.leave(ar -> {
        if (ar.failed()) {
          System.err.println("Failed to leave cluster " +
              ar.cause().getMessage());
          ar.cause().printStackTrace();
        }
        if (completionHandler != null) {
          runOnContext(v -> completionHandler.handle(Future.succeededFuture()));
        }
      });
    } else if (completionHandler != null) {
      runOnContext(v -> completionHandler.handle(Future.succeededFuture()));
    }
  }
}
