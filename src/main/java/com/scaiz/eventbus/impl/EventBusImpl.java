package com.scaiz.eventbus.impl;

import java.util.Objects;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Handler;
import com.scaiz.eventbus.DeliveryOptions;
import com.scaiz.eventbus.EventBus;
import com.scaiz.eventbus.Message;
import com.scaiz.eventbus.MessageCodec;
import com.scaiz.eventbus.MessageConsumer;
import com.scaiz.eventbus.MessageProducer;
import com.scaiz.eventbus.SendContext;

public class EventBusImpl implements EventBus {
  @Override
  public EventBus send(String address, Object message) {
    return null;
  }

  @Override
  public <T> EventBus send(String address, Object message, Handler<AsyncResult<Message<T>>> replyHandler) {
    return null;
  }

  @Override
  public EventBus send(String address, Object message, DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> EventBus send(String address, Object message, DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
    return null;
  }

  @Override
  public EventBus publish(String address, Object message) {
    return null;
  }

  @Override
  public EventBus publish(String address, Object message, DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> consumer(String address) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> consumer(String address, Handler<Message<T>> handler) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> localConsumer(String address) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> localConsumer(String address, Handler<Message<T>> handler) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> sender(String address) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> sender(String address, DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> publisher(String address) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> publisher(String address, DeliveryOptions options) {
    return null;
  }

  @Override
  public EventBus registerCodec(MessageCodec codec) {
    return null;
  }

  @Override
  public EventBus unregisterCodec(String name) {
    return null;
  }

  @Override
  public <T> EventBus registerDefaultCodec(Class<T> clazz, MessageCodec<T, ?> codec) {
    return null;
  }

  @Override
  public EventBus unregisterDefaultCodec(Class clazz) {
    return null;
  }

  @Override
  public void start(Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public EventBus addInterceptor(Handler<SendContext> interceptor) {
    return null;
  }

  @Override
  public EventBus removeInterceptor(Handler<SendContext> interceptor) {
    return null;
  }

  public <T> void removeRegistration(String address, HandlerRegistration<T> registration,
                                     Handler<AsyncResult<Void>> completionHandler) {
  }

  protected <T> void addRegistration(String address, HandlerRegistration<T> registration,
                                     boolean replyHandler, boolean localOnly) {
  }
}
