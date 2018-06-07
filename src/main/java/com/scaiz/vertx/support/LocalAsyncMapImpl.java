package com.scaiz.vertx.support;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
    Holder<V> prev = map.put(k, new Holder<>(v));
    if (prev != null && prev.expires()) {
      vertx.cancelTimer(prev.timerId);
    }
    completionHandler.handle(Future.succeededFuture());
  }

  @Override
  public void put(K k, V v, long timeout,
      Handler<AsyncResult<Void>> completionHandler) {
    long timestamp = System.nanoTime();
    long timeId = vertx.setTimer(timeout, l -> removeIfExpired(k));
    Holder<V> prev = map.put(k, new Holder<>(v, timeId, timeout, timestamp));
    if (prev != null && prev.expires()) {
      vertx.cancelTimer(prev.timerId);
    }
    completionHandler.handle(Future.succeededFuture());
  }

  private void removeIfExpired(K k) {
    map.computeIfPresent(k,
        (key, holder) -> holder.hasNotExpired() ? holder : null);
  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {
    Holder<V> h = map.putIfAbsent(k, new Holder<>(v));
    completionHandler.handle(
        Future.succeededFuture(h == null ? null : h.value));
  }

  @Override
  public void putIfAbsent(K k, V v, long timeout,
      Handler<AsyncResult<V>> completionHandler) {
    long timestamp = System.nanoTime();
    long timeId = vertx.setTimer(timeout, l -> removeIfExpired(k));
    Holder<V> prev = map.putIfAbsent(
        k, new Holder<>(v, timeId, timeout, timestamp));
    if (prev != null && prev.expires()) {
      vertx.cancelTimer(prev.timerId);
      completionHandler.handle(Future.succeededFuture(prev.value));
    }
    completionHandler.handle(Future.succeededFuture());
  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> resultHandler) {
    Holder<V> prev = map.remove(k);
    if (prev != null) {
      if (prev.expires()) {
        vertx.cancelTimer(prev.timerId);
      }
      resultHandler.handle(Future.succeededFuture(prev.value));
    } else {
      resultHandler.handle(Future.succeededFuture(null));
    }
  }

  @Override
  public void removeIfPresent(K k, V v,
      Handler<AsyncResult<Boolean>> resultHandler) {
    // why need to use atomic boolean ?
    // remapping function may called many times since
    // when the function called, the value may already changed
    // But still why need an atomic boolean, use compare and set without check
    // return value ?
    AtomicBoolean result = new AtomicBoolean();
    map.computeIfPresent(k, (key, holder) -> {
      if (holder.value.equals(v)) {
        result.compareAndSet(false, true);
        if (holder.expires()) {
          vertx.cancelTimer(holder.timerId);
        }
        return null;
      }
      return holder;
    });
    resultHandler.handle(Future.succeededFuture(result.get()));
  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> resultHandler) {
    Holder<V> previous = map.replace(k, new Holder<>(v));
    if (previous.expires()) {
      vertx.cancelTimer(previous.timerId);
      resultHandler.handle(Future.succeededFuture(previous.value));
    } else {
      resultHandler.handle(Future.succeededFuture(null));
    }
  }

  @Override
  public void replaceIfPresent(K k, V oldValue, V newValue,
      Handler<AsyncResult<Boolean>> resultHandler) {
    Holder<V> h = new Holder<>(newValue);
    Holder<V> result = map.computeIfPresent(k, (key, holder) -> {
      if (holder.value.equals(oldValue)) {
        if (holder.expires()) {
          vertx.cancelTimer(holder.timerId);
        }
        return h;
      }
      return holder;
    });
    resultHandler.handle(Future.succeededFuture(h == result));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    map.clear();
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(map.size()));
  }

  @Override
  public void keys(Handler<AsyncResult<Set<K>>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(map.keySet()));
  }

  @Override
  public void values(Handler<AsyncResult<List<V>>> resultHandler) {
    List<V> result = map.values().stream().filter(Holder::hasNotExpired)
        .map(h -> h.value).collect(Collectors.toList());
    resultHandler.handle(Future.succeededFuture(result));
  }

  @Override
  public void entries(Handler<AsyncResult<Map<K, V>>> resultHandler) {
    Map<K, V> result = new HashMap<>(map.size());
    map.forEach((key, holder) -> {
      if (holder.hasNotExpired()) {
        result.put(key, holder.value);
      }
    });
    resultHandler.handle(Future.succeededFuture(result));
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
