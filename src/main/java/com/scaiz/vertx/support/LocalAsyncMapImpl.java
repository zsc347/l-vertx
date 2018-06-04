package com.scaiz.vertx.support;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class LocalAsyncMapImpl<K, V> implements AsyncMap<K, V> {

  private final Vertx vertx;
  private final ConcurrentMap<K, Holder<V>> map;

  private LocalAsyncMapImpl(Vertx vertx) {
    this.vertx = vertx;
    map = new ConcurrentHashMap<>();
  }

  @Override
  public void get(K k, Handler<AsyncResult<V>> asyncResultHandler) {
    Holder<V> h = map.get(k);
    if (h != null && h.hasNotExpired()) {
      asyncResultHandler.handle(Future.succeededFuture(h.value));
    } else {
      asyncResultHandler.handle(Future.succeededFuture(null));
    }
  }

  @Override
  public void put(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
    
  }

  @Override
  public void put(K k, V v, long ttl,
      Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {

  }

  @Override
  public void putIfAbsent(K k, V v, long ttl,
      Handler<AsyncResult<V>> completionHandler) {

  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> asyncResultHandler) {

  }

  @Override
  public void removeIfPresent(K k, V v,
      Handler<AsyncResult<Boolean>> resultHandler) {

  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> asyncResultHandler) {

  }

  @Override
  public void replaceIfPresent(K k, V oldValue, V newValue,
      Handler<AsyncResult<Boolean>> resultHandler) {

  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {

  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {

  }

  @Override
  public void keys(Handler<AsyncResult<Set<K>>> asyncResultHandler) {

  }

  @Override
  public void values(Handler<AsyncResult<List<V>>> asyncResultHandler) {

  }

  @Override
  public void entries(Handler<AsyncResult<Map<K, V>>> asyncResultHandler) {

  }


  private static class Holder<V> {

    final V value;
    final long timerId;
    final long ttl;
    final long timestamp;

    Holder(V value) {
      Objects.requireNonNull(value);
      this.value = value;
      timestamp = ttl = timerId = 0;
    }

    Holder(V value, long timerId, long ttl, long timestamp) {
      Objects.requireNonNull(value);
      if (ttl < 1) {
        throw new IllegalArgumentException("ttl must be positive: " + ttl);
      }
      this.value = value;
      this.ttl = ttl;
      this.timerId = timerId;
      this.timestamp = timestamp;
    }

    boolean expires() {
      return ttl > 0;
    }

    boolean hasNotExpired() {
      return !expires() && TimeUnit.MILLISECONDS
          .convert(System.nanoTime() - timestamp, TimeUnit.NANOSECONDS) < ttl;
    }

    @Override
    public String toString() {
      return "Holder{" + "value=" + value + ", timerId = " + timerId + ", ttl="
          + ttl + ", timestamp=" + timestamp + "}";
    }
  }
}
