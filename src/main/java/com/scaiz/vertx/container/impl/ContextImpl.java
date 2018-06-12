package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Action;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.ContextTask;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.VertxThread;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public abstract class ContextImpl implements Context {

  private static final String DISABLE_TCCL_PROP_NAME = "vertx.disableTCCL";
  private static final boolean DISABLE_TCCL = Boolean
      .getBoolean(DISABLE_TCCL_PROP_NAME);

  private static final String DISABLE_TIMINGS_PROP_NAME
      = "vertx.disableContextTimings";
  private static final boolean DISABLE_TIMINGS
      = Boolean.getBoolean(DISABLE_TIMINGS_PROP_NAME);

  private final ClassLoader tccl;
  protected final EventLoop eventLoop;


  private CloseHooks closeHooks;
  private ConcurrentMap<Object, Object> contextData;
  private Handler<Throwable> exceptionHandler;

  final WorkerPool workerPool;
  final TaskQueue orderedTasks;

  private final WorkerPool internalWorkerPool;
  private final TaskQueue internalOrderedTasks;
  private Vertx owner;
  private VertxThread contextThread;

  ContextImpl(VertxInternal vertx, ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    this.tccl = tccl;
    this.closeHooks = new CloseHooks();

    this.internalWorkerPool = internalWorkerPool;
    this.internalOrderedTasks = new TaskQueue();

    this.workerPool = workerPool;
    this.orderedTasks = new TaskQueue();

    this.owner = vertx;
    this.eventLoop = getEventLoop(vertx);
  }

  ContextImpl(VertxInternal vertx, EventLoop eventLoop, ClassLoader tccl,
      WorkerPool internalWorkerPool,
      WorkerPool workerPool) {
    this.tccl = tccl;
    this.closeHooks = new CloseHooks();

    this.internalWorkerPool = internalWorkerPool;
    this.internalOrderedTasks = new TaskQueue();

    this.workerPool = workerPool;
    this.orderedTasks = new TaskQueue();

    this.owner = vertx;
    this.eventLoop = eventLoop;
  }


  public EventLoop nettyEventLoop() {
    return eventLoop;
  }

  private static EventLoop getEventLoop(VertxInternal vertx) {
    EventLoopGroup group = vertx.getEventLoopGroup();
    if (group != null) {
      return group.next();
    } else {
      return null;
    }
  }

  public static void setCurrentThreadContext(ContextImpl context) {
    Thread current = Thread.currentThread();
    if (current instanceof VertxThread) {
      ((VertxThread) current).setContext(context);
      if (!DISABLE_TCCL) {
        context.setTCCLToCurrentThread();
      } else {
        Thread.currentThread().setContextClassLoader(null);
      }
    } else {
      throw new IllegalStateException(
          "Attempt to setCurrentThreadContext on non Vert.x thread "
              + Thread.currentThread());
    }
  }

  private void setTCCLToCurrentThread() {
    Thread.currentThread().setContextClassLoader(tccl);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void put(String key, Object value) {
    contextData().put(key, value);
  }

  @Override
  public boolean remove(String key) {
    return contextData().remove(key) != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) contextData().get(key);
  }

  private synchronized ConcurrentMap contextData() {
    if (contextData == null) {
      contextData = new ConcurrentHashMap<>();
    }
    return contextData;
  }

  @Override
  public void runOnContext(Handler<Void> action) {
    try {
      executeAsync(action);
    } catch (RejectedExecutionException e) {
      // pool has been shutdown
    }
  }

  protected abstract void executeAsync(Handler<Void> task);

  public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      TaskQueue queue, Handler<AsyncResult<T>> resultHandler) {
    executeBlocking(null, blockingCodeHandler, workerPool.executor(),
        queue, resultHandler);
  }

  @Override
  public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      boolean ordered, Handler<AsyncResult<T>> resultHandler) {
    executeBlocking(null, blockingCodeHandler, workerPool.executor(),
        ordered ? orderedTasks : null, resultHandler);
  }

  private <T> void executeBlocking(Action<T> action,
      Handler<Future<T>> blockingCodeHandler,
      Executor exec,
      TaskQueue queue,
      Handler<AsyncResult<T>> resultHandler) {
    Runnable command = () -> {
      VertxThread current = (VertxThread) Thread.currentThread();
      if (!DISABLE_TIMINGS) {
        current.executeStart();
      }
      Future<T> res = Future.future();
      try {
        if (blockingCodeHandler != null) {
          ContextImpl.setCurrentThreadContext(this);
          blockingCodeHandler.handle(res);
        } else {
          T result = action.perform();
          res.complete(result);
        }
      } catch (Throwable t) {
        res.fail(t);
      } finally {
        if (!DISABLE_TIMINGS) {
          current.executeEnd();
        }
      }
      if (resultHandler != null) {
        runOnContext((v) -> res.setHandler(resultHandler));
      }
    };

    if (queue != null) {
      queue.execute(command, exec);
    } else {
      exec.execute(command);
    }
  }


  Runnable wrapTask(ContextTask cTask, Handler<Void> hTask) {
    return () -> {
      Thread tc = Thread.currentThread();
      if (!(tc instanceof VertxThread)) {
        throw new IllegalStateException(
            "Uh oh! Event loop context executing with wrong thread! Expected "
                + contextThread + " got " + tc);
      }
      VertxThread current = (VertxThread) tc;
      if (!DISABLE_TIMINGS) {
        current.executeStart();
      }
      try {
        setCurrentThreadContext(ContextImpl.this);
        if (cTask != null) {
          cTask.run();
        } else {
          hTask.handle(null);
        }
      } catch (Throwable t) {
        Handler<Throwable> handler = this.exceptionHandler;
        if (handler == null) {
          handler = owner.exceptionHandler();
        }
        if (handler != null) {
          handler.handle(t);
        } else {
          System.err.println("Error occur in context " + t.getMessage());
          t.printStackTrace();
        }
      } finally {
        if (!DISABLE_TIMINGS) {
          current.executeEnd();
        }
      }
    };
  }


  @Override
  public void addCloseHook(Closeable hook) {
    closeHooks.add(hook);
  }

  @Override
  public void removeCloseHook(Closeable hook) {
    closeHooks.remove(hook);
  }


  @Override
  public Context exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  @Override
  public Handler<Throwable> exceptionHandler() {
    return this.exceptionHandler;
  }

  @Override
  public Vertx owner() {
    return owner;
  }

  public void runCloseHooks(Handler<AsyncResult<Void>> completeHandler) {
    closeHooks.run(completeHandler);
    VertxThreadFactory.unsetContext(this);
  }

  public static boolean isOnWorkerThread() {
    return isOnVertxThread(true);
  }

  public static boolean isOnEventLoopThread() {
    return isOnVertxThread(false);
  }

  private static boolean isOnVertxThread(boolean isWorker) {
    Thread t = Thread.currentThread();
    if (t instanceof VertxThread) {
      VertxThread vt = (VertxThread) t;
      return vt.isWorker() == isWorker;
    }
    return false;
  }

  public void executeFromIO(ContextTask task) {
    wrapTask(task, null).run();
  }
}
