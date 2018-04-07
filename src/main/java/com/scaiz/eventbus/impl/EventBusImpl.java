package com.scaiz.eventbus.impl;

import com.scaiz.Context;
import com.scaiz.Vertx;
import com.scaiz.async.AsyncResult;
import com.scaiz.async.Closeable;
import com.scaiz.async.Future;
import com.scaiz.async.Handler;
import com.scaiz.eventbus.DeliveryOptions;
import com.scaiz.eventbus.EventBus;
import com.scaiz.eventbus.HandlerHolder;
import com.scaiz.eventbus.Handlers;
import com.scaiz.eventbus.Message;
import com.scaiz.eventbus.MessageCodec;
import com.scaiz.eventbus.MessageConsumer;
import com.scaiz.eventbus.MessageProducer;
import com.scaiz.eventbus.SendContext;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBusImpl implements EventBus {

  protected final ConcurrentMap<String, Handlers> handlerMap = new ConcurrentHashMap<>();
  private final List<Handler<SendContext>> interceptors = new CopyOnWriteArrayList<>();
  private Vertx vertx;


  @Override
  public EventBus send(String address, Object message) {
    return null;
  }

  @Override
  public <T> EventBus send(String address, Object message,
      Handler<AsyncResult<Message<T>>> replyHandler) {
    return null;
  }

  @Override
  public EventBus send(String address, Object message,
      DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> EventBus send(String address, Object message,
      DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
    return null;
  }

  @Override
  public EventBus publish(String address, Object message) {
    return null;
  }

  @Override
  public EventBus publish(String address, Object message,
      DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> consumer(String address) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> consumer(String address,
      Handler<Message<T>> handler) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> localConsumer(String address) {
    return null;
  }

  @Override
  public <T> MessageConsumer<T> localConsumer(String address,
      Handler<Message<T>> handler) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> sender(String address) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> sender(String address,
      DeliveryOptions options) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> publisher(String address) {
    return null;
  }

  @Override
  public <T> MessageProducer<T> publisher(String address,
      DeliveryOptions options) {
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
  public <T> EventBus registerDefaultCodec(Class<T> clazz,
      MessageCodec<T, ?> codec) {
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
    interceptors.add(interceptor);
    return this;
  }

  @Override
  public EventBus removeInterceptor(Handler<SendContext> interceptor) {
    interceptors.remove(interceptor);
    return this;
  }

  public <T> void removeRegistration(String address,
      HandlerRegistration<T> registration,
      Handler<AsyncResult<Void>> completionHandler) {

  }

  protected <T> void addRegistration(String address,
      HandlerRegistration<T> registration,
      boolean replyHandler, boolean localOnly) {
    Objects.requireNonNull(registration.getHandler(), "handler");
    addLocalRegistration(address, registration,
        replyHandler, localOnly);
    registration.setResult(Future.succeededFuture());
  }

  private <T> boolean addLocalRegistration(String address,
      HandlerRegistration<T> registration, boolean replyHandler,
      boolean localOnly) {
    Objects.requireNonNull(address, "address");
    Context context = vertx.currentContext();
    boolean hasContext = context != null;
    if (!hasContext) {
      context = vertx.getOrCreateContext();
    }
    registration.setHandlerContext(context);

    boolean newAddress = false;
    HandlerHolder holder = new HandlerHolder<>(registration, replyHandler,
        localOnly, context);

    Handlers handlers = handlerMap.get(address);

    if (handlers == null) {
      handlers = new Handlers();
      Handlers preHandlers = handlerMap.putIfAbsent(address, handlers);
      if (preHandlers != null) {
        handlers = preHandlers;
      }
      newAddress = true;
    }
    handlers.list.add(holder);

    if (hasContext) {
      HandlerEntry entry = new HandlerEntry<>(address, registration);
      context.addCloseHook(entry);
    }

    return newAddress;
  }

  static class HandlerEntry<T> implements Closeable {

    final String address;
    final HandlerRegistration<T> handler;

    HandlerEntry(String address,
        HandlerRegistration<T> handler) {
      this.address = address;
      this.handler = handler;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      HandlerEntry that = (HandlerEntry) o;
      if (!address.equals(that.address) || !handler.equals(that.handler)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = address != null ? address.hashCode() : 0;
      result = 31 * result + (handler != null ? handler.hashCode() : 0);
      return result;
    }


    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
      handler.unregister(completionHandler);
    }
  }

}
