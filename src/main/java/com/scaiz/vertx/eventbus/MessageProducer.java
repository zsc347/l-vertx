package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.streams.WriteStream;

public interface MessageProducer<T> extends WriteStream<T> {

  int DEFAULT_WRITE_QUEUE_MAX_SIZE = 1000;

  default MessageProducer<T> send(T message) {
    return send(message, null);
  }

  <R> MessageProducer<T> send(T message,
      Handler<AsyncResult<Message<R>>> replyHandler);

  @Override
  MessageProducer<T> exceptionHandler(Handler<Throwable> handler);

  @Override
  MessageProducer<T> write(T data);

  @Override
  MessageProducer<T> setWriteQueueMaxSize(int maxSize);

  @Override
  MessageProducer<T> drainHandler(Handler<Void> handler);

  MessageProducer<T> deliveryOptions(DeliveryOptions options);

  String address();

  @Override
  void end();

  void close();
}
