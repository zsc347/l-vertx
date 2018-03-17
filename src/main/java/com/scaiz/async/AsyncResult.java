package com.scaiz.async;

import java.util.function.Function;

public interface AsyncResult<T> {

  T result();

  Throwable cause();

  boolean succeeded();

  boolean failed();

  default <U> AsyncResult<U> map(Function<T, U> mapper) {
    return new AsyncResult<U>() {
      @Override
      public U result() {
        return mapper.apply(AsyncResult.this.result());
      }

      @Override
      public Throwable cause() {
        return AsyncResult.this.cause();
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded();
      }

      @Override
      public boolean failed() {
        return AsyncResult.this.failed();
      }
    };
  }

  default <V> AsyncResult<V> map(V v) {
    return map(t -> v);
  }

  default <V> AsyncResult<V> mapEmpty() {
    return map((V) null);
  }

  default AsyncResult<T> otherwise(Function<Throwable, T> mapper) {
    return new AsyncResult<T>() {
      @Override
      public T result() {
        if (AsyncResult.this.succeeded()) {
          return AsyncResult.this.result();
        } else if (AsyncResult.this.failed()) {
          return mapper.apply(AsyncResult.this.cause());
        }
        return null;
      }

      @Override
      public Throwable cause() {
        return null;
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded() || AsyncResult.this.failed();
      }

      @Override
      public boolean failed() {
        return false;
      }
    };
  }

  default AsyncResult<T> otherwise(T v) {
    return otherwise(error -> v);
  }

  default AsyncResult<T> otherwiseEmpty() {
    return otherwise(error -> null);
  }
}
