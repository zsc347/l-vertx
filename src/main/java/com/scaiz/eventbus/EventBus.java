package com.scaiz.eventbus;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Handler;

public interface EventBus {

  EventBus send(String address, Object message);

  <T> EventBus send(String address, Object message,
      Handler<AsyncResult<Message<T>>> replyHandler);

  EventBus send(String address, Object message, DeliveryOptions options);

  <T> EventBus send(String address, Object message, DeliveryOptions options,
      Handler<AsyncResult<Message<T>>> replyHandler);

  EventBus publish(String address, Object message);

  EventBus publish(String address, Object message, DeliveryOptions options);

  <T> MessageConsumer<T> consumer(String address);

  <T> MessageConsumer<T> consumer(String address, Handler<Message<T>> handler);

  <T> MessageConsumer<T> localConsumer(String address);

  <T> MessageConsumer<T> localConsumer(String address,
      Handler<Message<T>> handler);

  <T> MessageProducer<T> sender(String address);

  <T> MessageProducer<T> sender(String address, DeliveryOptions options);

  <T> MessageProducer<T> publisher(String address);

  <T> MessageProducer<T> publisher(String address, DeliveryOptions options);

  EventBus registerCodec(MessageCodec codec);

  EventBus unregisterCodec(String name);

  <T> EventBus registerDefaultCodec(Class<T> clazz, MessageCodec<T, ?> codec);

  EventBus unregisterDefaultCodec(Class clazz);

  void start(Handler<AsyncResult<Void>> completionHandler);

  void close(Handler<AsyncResult<Void>> completionHandler);

  EventBus addInterceptor(Handler<SendContext> interceptor);

  EventBus removeInterceptor(Handler<SendContext> interceptor);
}
