package com.scaiz.vertx.container;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.impl.TaskQueue;

public interface Context {

  void put(String key, Object value);

  boolean remove(String key);

  <T> T get(String key);

  void runOnContext(Handler<Void> action);

  /**
   * Future pass to blockingCodeHandler is an reference. blockingCodeHandler
   * should call {@link Future#complete} when it runs successfully, otherwise
   * call {@link Future#fail}
   */
  <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      boolean ordered, Handler<AsyncResult<T>> resultHandler);

  default <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      Handler<AsyncResult<T>> resultHandler) {
    executeBlocking(blockingCodeHandler, true, resultHandler);
  }

  void addCloseHook(Closeable hook);

  void removeCloseHook(Closeable hook);

  Context exceptionHandler(Handler<Throwable> handler);

  Handler<Throwable> exceptionHandler();

  Vertx owner();
}
