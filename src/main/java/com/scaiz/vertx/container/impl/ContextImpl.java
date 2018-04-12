package com.scaiz.vertx.container.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;

public class ContextImpl implements Context {

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

  }

  @Override
  public void removeCloseHook(Closeable hook) {

  }

  @Override
  public Context exceptionHandler(Handler<Throwable> handler) {
    return null;
  }
}
