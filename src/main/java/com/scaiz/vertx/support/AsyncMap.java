package com.scaiz.vertx.support;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AsyncMap<K,V> {

  void get(K k, Handler<AsyncResult<V>> resultHandler);

  void put(K k, V v, Handler<AsyncResult<Void>> completionHandler);

  void put(K k, V v, long ttl, Handler<AsyncResult<Void>> completionHandler);

  void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler);

  void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> completionHandler);

  void remove(K k, Handler<AsyncResult<V>> resultHandler);

  void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> resultHandler);

  void replace(K k, V v, Handler<AsyncResult<V>> resultHandler);

  void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler);

  void clear(Handler<AsyncResult<Void>> resultHandler);

  void size(Handler<AsyncResult<Integer>> resultHandler);

  void keys(Handler<AsyncResult<Set<K>>> resultHandler);

  void values(Handler<AsyncResult<List<V>>> resultHandler);

  void entries(Handler<AsyncResult<Map<K, V>>> resultHandler);
}
