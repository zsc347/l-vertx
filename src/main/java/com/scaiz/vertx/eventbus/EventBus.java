package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;

public interface EventBus {

  <T> EventBus send(String address, Object message, DeliveryOptions options,
      Handler<AsyncResult<Message<T>>> replyHandler);

  default EventBus send(String address, Object message) {
    return send(address, message, new DeliveryOptions(), null);
  }

  default <T> EventBus send(String address, Object message,
      Handler<AsyncResult<Message<T>>> replyHandler) {
    return send(address, message, new DeliveryOptions(), replyHandler);
  }

  default EventBus send(String address, Object message,
      DeliveryOptions options) {
    return send(address, message, options, null);
  }

  EventBus publish(String address, Object message, DeliveryOptions options);

  default EventBus publish(String address, Object message) {
    return publish(address, message, new DeliveryOptions());
  }

  <T> MessageConsumer<T> consumer(String address,
      Handler<Message<T>> handler);

  default <T> MessageConsumer<T> consumer(String address) {
    return consumer(address, null);
  }

  <T> MessageProducer<T> sender(String address, DeliveryOptions options);

  default <T> MessageProducer<T> sender(String address) {
    return sender(address, new DeliveryOptions());
  }

  <T> MessageProducer<T> publisher(String address, DeliveryOptions options);

  default <T> MessageProducer<T> publisher(String address) {
    return publisher(address, new DeliveryOptions());
  }

  EventBus registerCodec(MessageCodec codec);

  EventBus unregisterCodec(String name);

  void start(Handler<AsyncResult<Void>> completionHandler);

  void close(Handler<AsyncResult<Void>> completionHandler);

  EventBus addInterceptor(Handler<SendContext> interceptor);

  EventBus removeInterceptor(Handler<SendContext> interceptor);
}
