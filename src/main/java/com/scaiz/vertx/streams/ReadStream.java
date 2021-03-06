package com.scaiz.vertx.streams;

import com.scaiz.vertx.async.Handler;

public interface ReadStream<T> extends StreamBase {

  @Override
  ReadStream<T> exceptionHandler(Handler<Throwable> handler);

  ReadStream<T> handler(Handler<T> handler);

  ReadStream<T> pause();

  ReadStream<T> resume();

  ReadStream<T> endHandler(Handler<Void> endHandler);

}
