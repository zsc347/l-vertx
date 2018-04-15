package com.scaiz.vertx.mock;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Handler;

public class ContextMock implements Context {

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
    action.handle(null);

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