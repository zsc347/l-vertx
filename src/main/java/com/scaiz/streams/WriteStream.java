package com.scaiz.streams;

import com.scaiz.async.Handler;

public interface WriteStream<T> extends StreamBase {

  @Override
  WriteStream<T> exceptionHandler(Handler<Throwable> handler);

  WriteStream<T> write(T data);

  void end();

  WriteStream<T> setWriteQueueMaxSize(int maxSize);

  boolean writeQueueFull();

  WriteStream<T> drainHandler(Handler<Void> handler);
}
