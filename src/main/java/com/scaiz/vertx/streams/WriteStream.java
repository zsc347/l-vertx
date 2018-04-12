package com.scaiz.vertx.streams;

import com.scaiz.vertx.async.Handler;

public interface WriteStream<T> extends StreamBase {

  @Override
  WriteStream<T> exceptionHandler(Handler<Throwable> handler);

  WriteStream<T> write(T data);

  void end();

  WriteStream<T> setWriteQueueMaxSize(int maxSize);

  boolean writeQueueFull();

  WriteStream<T> drainHandler(Handler<Void> handler);
}
