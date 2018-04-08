package com.scaiz.eventbus;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Handler;
import com.scaiz.support.MultiMap;

public interface Message<T> {

  String address();

  String replyAddress();

  T body();

  MultiMap headers();

  boolean isSend();

  default void reply(Object message) {
    reply(message, new DeliveryOptions(), null);
  }

  default <R> void reply(Object message,
      Handler<AsyncResult<Message<R>>> replyHandler) {
    reply(message, new DeliveryOptions(), replyHandler);
  }

  default void reply(Object message, DeliveryOptions options) {
    reply(message, options, null);
  }

  <R> void reply(Object message, DeliveryOptions options,
      Handler<AsyncResult<Message<R>>> replyHandler);

  void fail(int failureCode, String message);
}
