package com.scaiz.vertx.support;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import java.util.function.Predicate;

public interface AsyncMultiMap<K, V> {

  void add(K k, V v, Handler<AsyncResult<Void>> completionHandler);

  void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> resultHandler);

  void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler);

  void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler);

  void removeAllMatching(Predicate<V> p,
      Handler<AsyncResult<Void>> completionHandler);
}
