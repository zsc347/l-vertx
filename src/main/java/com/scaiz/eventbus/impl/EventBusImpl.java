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
import com.scaiz.eventbus.ReplyFailure;
import com.scaiz.eventbus.SendContext;
import com.scaiz.support.MultiMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBusImpl implements EventBus {

  private final ConcurrentMap<String, Handlers> handlerMap =
      new ConcurrentHashMap<>();
  private final List<Handler<SendContext>> interceptors =
      new CopyOnWriteArrayList<>();
  protected final CodecManager codecManager = new CodecManager();

  private final Vertx vertx;

  private boolean started;

  public EventBusImpl(Vertx vertx) {
    this.vertx = vertx;
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
  public synchronized void start(Handler<AsyncResult<Void>> completionHandler) {
    if (started) {
      throw new IllegalStateException("Already started");
    }
    started = true;
    completionHandler.handle(Future.succeededFuture());
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    assertStarted();
    unregisterAll();

    if (completionHandler != null) {
      vertx.runOnContext(
          v -> completionHandler.handle(Future.succeededFuture()));
    }
  }

  private void unregisterAll() {
    handlerMap.values()
        .forEach(handlers ->
            handlers.list.forEach(handlerHolder -> {
              handlerHolder.getHandler().unregister();
            }));
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
    removeLocalRegistration(address, registration);
    if (completionHandler != null) {
      vertx.runOnContext(
          v -> completionHandler.handle(Future.succeededFuture()));
    }

  }


  @SuppressWarnings("unchecked")
  private <T> void removeLocalRegistration(String address,
      HandlerRegistration<T> handler) {
    Handlers handlers = handlerMap.get(address);
    if (handlers != null) {
      synchronized (handlers) {
        int size = handlers.list.size();
        for (int i = 0; i < size; i++) {
          HandlerHolder holder = handlers.list.get(i);
          if (holder.getHandler() == handler) {
            handlers.list.remove(i);
            holder.setRemoved();
            if (handlers.list.isEmpty()) {
              handlerMap.remove(address);
            }
            holder.getContext().removeCloseHook(
                new HandlerEntry<>(address, holder.getHandler()));
          }
        }
      }
    }
  }

  protected <T> void addRegistration(String address,
      HandlerRegistration<T> registration,
      boolean replyHandler, boolean localOnly) {
    Objects.requireNonNull(registration.getHandler(), "handler");
    addLocalRegistration(address, registration,
        replyHandler, localOnly);
    registration.setResult(Future.succeededFuture());
  }

  private <T> void addLocalRegistration(String address,
      HandlerRegistration<T> registration, boolean replyHandler,
      boolean localOnly) {
    Objects.requireNonNull(address, "address");
    Context context = vertx.currentContext();
    boolean hasContext = context != null;
    if (!hasContext) {
      context = vertx.getOrCreateContext();
    }
    registration.setHandlerContext(context);

    HandlerHolder holder = new HandlerHolder<>(registration, replyHandler,
        localOnly, context);

    Handlers handlers = handlerMap.get(address);

    if (handlers == null) {
      handlers = new Handlers();
      Handlers preHandlers = handlerMap.putIfAbsent(address, handlers);
      if (preHandlers != null) {
        handlers = preHandlers;
      }
    }
    handlers.list.add(holder);

    if (hasContext) {
      HandlerEntry entry = new HandlerEntry<>(address, registration);
      context.addCloseHook(entry);
    }

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
      return address.equals(that.address) && handler.equals(that.handler);
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

  private void assertStarted() {
    if (!started) {
      throw new IllegalStateException("Event Bus is not started");
    }
  }

  MessageImpl createMessage(boolean isSend, String address, MultiMap headers,
      Object body, String codecName) {
    Objects.requireNonNull(address, "address");
    MessageCodec codec = codecManager.lookupCodec(body, codecName);
    @SuppressWarnings("unchecked")
    MessageImpl msg = new MessageImpl(address, null, headers, body,
        codec, isSend, this);
    return msg;
  }

  private <T> void sendOrPub(SendContextImpl<T> sendContext) {
    deliverMessageLocally(sendContext);
  }

  private <T> void deliverMessageLocally(SendContextImpl<T> sendContext) {
    if (!deliverMessageLocally(sendContext.message)) {
      if (sendContext.handlerRegistration != null) {
        sendContext.handlerRegistration.sendAsyncResultFailure(
            ReplyFailure.NO_HANDLERS,
            "No handlers for address " + sendContext.message.address());
      }
    }
  }

  private boolean deliverMessageLocally(MessageImpl msg) {
    msg.setBus(this);
    Handlers handlers = handlerMap.get(msg.address());
    if (handlers != null) {
      if (msg.isSend()) {
        HandlerHolder holder = handlers.choose();
        if (holder != null) {
          deliverToHandler(msg, holder);
        }
      } else {
        for (HandlerHolder holder : handlers.list) {
          deliverToHandler(msg, holder);
        }
      }
      return true;
    }
    return false;
  }

  private <T> void deliverToHandler(MessageImpl msg, HandlerHolder<T> holder) {
    @SuppressWarnings("unchecked")
    Message<T> copied = msg.copyBeforeReceive();
    holder.getContext().runOnContext(v -> {
      try {
        // handler might be removed after message send but before it was received
        if (!holder.isRemoved()) {
          holder.getHandler().handle(copied);
        }
      } finally {
        if (holder.isReplyHander()) {
          holder.getHandler().unregister();
        }
      }
    });
  }


  class SendContextImpl<T> implements SendContext<T> {

    public final MessageImpl message;
    public final DeliveryOptions options;
    public final HandlerRegistration handlerRegistration;
    public final Iterator<Handler<SendContext>> iter;

    SendContextImpl(MessageImpl message,
        DeliveryOptions options,
        HandlerRegistration handlerRegistration,
        Iterator<Handler<SendContext>> iter) {
      this.message = message;
      this.options = options;
      this.handlerRegistration = handlerRegistration;
      this.iter = iter;
    }

    @Override
    public Message<T> message() {
      return message;
    }

    @Override
    public void next() {
      if (iter.hasNext()) {
        Handler<SendContext> handler = iter.next();
        try {
          handler.handle(this);
        } catch (Throwable t) {
          System.err.println("Failure in interceptor" + t.getMessage());
        }
      } else {
        sendOrPub(this);
      }
    }

    @Override
    public boolean isSend() {
      return message.isSend();
    }

    @Override
    public Object sentBody() {
      return message.sendBody();
    }
  }
}
