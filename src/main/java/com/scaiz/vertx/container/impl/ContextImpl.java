package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.container.VertxThread;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

import com.scaiz.vertx.VertxInternal;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import java.util.concurrent.ConcurrentMap;

public abstract class ContextImpl implements Context {

  private static final String DISABLE_TCCL_PROP_NAME = "vertx.disableTCCL";
  private static final boolean DISABLE_TCCL = Boolean
      .getBoolean(DISABLE_TCCL_PROP_NAME);

  private final ClassLoader tccl;
  private CloseHooks closeHooks;
  private ConcurrentMap<Object, Object> contextData;

  protected ContextImpl(ClassLoader tccl) {
    this.tccl = tccl;
  }

  private static EventLoop getEventLoop(VertxInternal vertx) {
    EventLoopGroup group = vertx.getEventLoopGroup();
    if (group != null) {
      return group.next();
    } else {
      return null;
    }
  }

  public static void setContext(ContextImpl context) {
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
          "Attempt to setContext on non Vert.x thread "
              + Thread.currentThread());
    }
  }

  private void setTCCLToCurrentThread() {
    Thread.currentThread().setContextClassLoader(tccl);
  }

  @Override
  public void put(String key, Object value) {

  }

  @Override
  public boolean remove(String key) {
    return false;
  }

  @Override
  public <T> T get(String key) {
    return null;
  }

  @Override
  public void runOnContext(Handler<Void> action) {

  }

  @Override
  public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      boolean ordered, Handler<AsyncResult<T>> asyncResultHandler) {

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
  public Context exceptionHandler(Handler<Throwable> handler) {
    return null;
  }
}
