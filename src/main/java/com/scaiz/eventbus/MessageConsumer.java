package com.scaiz.eventbus;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Handler;
import com.scaiz.streams.ReadStream;

public interface MessageConsumer<T> extends ReadStream<Message<T>> {

  @Override
  MessageConsumer<T> exceptionHandler(Handler<Throwable> handler);

  @Override
  MessageConsumer<T> handler(Handler<Message<T>> handler);

  @Override
  MessageConsumer<T> pause();

  @Override
  MessageConsumer<T> resume();

  @Override
  MessageConsumer<T> endHandler(Handler<Void> endHandler);

  ReadStream<T> bodyStream();

  boolean isRegistered();

  String address();

  MessageConsumer<T> setMaxBufferedMessages(int maxBufferedMessages);

  int getMaxBufferedMessages();

  void completionHandler(Handler<AsyncResult<Void>> completionHandler);

  default void unregister() {
    unregister(null);
  }

  void unregister(Handler<AsyncResult<Void>> completionHandler);
}
