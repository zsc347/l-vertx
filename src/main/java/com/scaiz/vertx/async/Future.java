package com.scaiz.vertx.async;

import com.scaiz.vertx.spi.ServiceHelper;
import java.util.function.Function;

public interface Future<T> extends AsyncResult<T>, Handler<AsyncResult<T>> {

  static <T> Future<T> future() {
    return factory.future();
  }

  static <T> Future<T> future(Handler<Future<T>> handler) {
    Future<T> fut = future();
    handler.handle(fut);
    return fut;
  }

  static <T> Future<T> succeededFuture() {
    return factory.succeededFuture();
  }

  static <T> Future<T> succeededFuture(T result) {
    return factory.succeededFuture(result);
  }

  static <T> Future<T> failedFuture(Throwable t) {
    return factory.failFuture(t);
  }

  boolean isComplete();

  Future<T> setHandler(Handler<AsyncResult<T>> handler);

  void complete(T result);

  void fail(Throwable cause);

  boolean tryFail(Throwable cause);

  default <U> Future<U> compose(Handler<T> handler, Future<U> next) {
    setHandler(ar -> {
      if (ar.succeeded()) {
        try {
          handler.handle(ar.result());
        } catch (Throwable err) {
          if (next.isComplete()) {
            throw err;
          }
          next.fail(err);
        }
      } else {
        next.fail(ar.cause());
      }
    });
    return next;
  }

  default <U> Future<U> compose(Function<T, Future<U>> mapper) {
    if (mapper == null) {
      throw new NullPointerException();
    }
    Future<U> ret = Future.future();
    setHandler(ar -> {
      if (ar.succeeded()) {
        Future<U> apply;
        try {
          apply = mapper.apply(ar.result());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        apply.setHandler(ret);
      } else {
        ret.fail(ar.cause());
      }
    });
    return ret;
  }


  default <U> Future<U> map(Function<T, U> mapper) {
    Future<U> ret = Future.future();

    setHandler(ar -> {
      if (ar.succeeded()) {
        U mapped;
        try {
          mapped = mapper.apply(ar.result());
        } catch (Throwable e) {
          ret.fail(e);
          return;
        }
        ret.complete(mapped);
      } else {
        ret.fail(ar.cause());
      }
    });

    return ret;
  }


  @Override
  default <V> Future<V> map(V v) {
    Future ret = Future.future();
    setHandler(ar -> {
      if (ar.succeeded()) {
        ret.complete(v);
      } else {
        ret.fail(ar.cause());
      }
    });
    return ret;
  }

  @Override
  default <V> Future<V> mapEmpty() {
    return (Future<V>) mapEmpty();
  }


  @Override
  void handle(AsyncResult<T> asyncResult);


  default Handler<AsyncResult<T>> completer() {
    return this;
  }


  default Future<T> recover(Function<Throwable, Future<T>> mapper) {
    Future<T> ret = Future.future();

    setHandler(ar -> {
      if (ar.succeeded()) {
        ret.complete(result());
      } else {
        Future<T> mapped;
        try {
          mapped = mapper.apply(ar.cause());
        } catch (Throwable err) {
          ret.fail(err);
          return;
        }
        mapped.setHandler(ret);
      }
    });

    return ret;
  }

  default Future<T> otherwise(Function<Throwable, T> mapper) {
    Future<T> ret = Future.future();

    setHandler(ar -> {
      if (ar.succeeded()) {
        ret.complete(result());
      } else {
        T value;
        try {
          value = mapper.apply(ar.cause());
        } catch (Throwable err) {
          ret.fail(err);
          return;
        }
        ret.complete(value);
      }
    });

    return ret;
  }

  default Future<T> otherwise(T value) {
    return otherwise(err -> value);
  }

  default Future<T> otherwiseEmpty() {
    return (Future<T>) AsyncResult.super.otherwiseEmpty();
  }

  FutureFactory factory = ServiceHelper.loadFactory(FutureFactory.class);
}
