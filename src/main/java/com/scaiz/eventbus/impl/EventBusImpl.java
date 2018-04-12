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
import com.scaiz.eventbus.ReplyException;
import com.scaiz.eventbus.ReplyFailure;
import com.scaiz.eventbus.SendContext;
import com.scaiz.support.MultiMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class EventBusImpl implements EventBus {

  private final AtomicLong replySequence = new AtomicLong(0);

  private final ConcurrentMap<String, Handlers> handlerMap =
      new ConcurrentHashMap<>();
  private final List<Handler<SendContext>> interceptors =
      new CopyOnWriteArrayList<>();
  private final CodecManager codecManager = new CodecManager();

  private final Vertx vertx;

  private boolean started;

  public EventBusImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  private <T> void sendOrPubInternal(MessageImpl message,
      DeliveryOptions options,
      Handler<AsyncResult<Message<T>>> replyHandler) {
    assertStarted();
    HandlerRegistration<T> replyHandlerRegistration = createReplyHandlerRegistration(
        message, options, replyHandler);
    new SendContextImpl<>(message, options, replyHandlerRegistration).next();
  }

  @Override
  public <T> EventBus send(String address, Object message,
      DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
    sendOrPubInternal(
        createMessage(true, address, options.getHeaders(), message,
            options.getCodecName()), options, replyHandler);
    return this;
  }


  @Override
  public EventBus publish(String address, Object message,
      DeliveryOptions options) {
    sendOrPubInternal(createMessage(false, address, options.getHeaders(),
        message, options.getCodecName()), options, null);
    return this;
  }


  @Override
  public <T> MessageConsumer<T> consumer(String address,
      Handler<Message<T>> handler) {
    assertStarted();
    MessageConsumer<T> consumer = new HandlerRegistration<>(vertx, address,
        null, this, null, -1);
    if (handler != null) {
      consumer.handler(handler);
    }
    return consumer;
  }

  @Override
  public <T> MessageProducer<T> sender(String address,
      DeliveryOptions options) {
    return new MessageProducerImpl<>(vertx, address, true, options);
  }

  @Override
  public <T> MessageProducer<T> publisher(String address,
      DeliveryOptions options) {
    return new MessageProducerImpl<>(vertx, address, false, options);
  }

  @Override
  public EventBus registerCodec(MessageCodec codec) {
    codecManager.registerCodec(codec);
    return this;
  }

  @Override
  public EventBus unregisterCodec(String name) {
    codecManager.unregisterCodec(name);
    return this;
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
          break;
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

  public <T> void sendReply(MessageImpl replyMessage,
      DeliveryOptions options,
      Handler<AsyncResult<Message<T>>> replyHandler) {
    if (replyMessage.address() == null) {
      throw new IllegalStateException("address not specified");
    } else {
      HandlerRegistration<T> replyHandlerRegistration = createReplyHandlerRegistration(
          replyMessage, options, replyHandler);
      new SendContextImpl<>(replyMessage, options, replyHandlerRegistration)
          .next();
    }
  }

  private <T> HandlerRegistration<T> createReplyHandlerRegistration(
      MessageImpl message,
      DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
    if (replyHandler != null) {
      long timeout = options.getSendTimeout();
      String replyAddress = generateReplyAddress();
      message.setReplyAddress(replyAddress);
      Handler<Message<T>> simpleReplyHandler = convertHandler(replyHandler);
      HandlerRegistration<T> registration = new HandlerRegistration<>(vertx,
          replyAddress, message.address(), this, replyHandler, timeout);
      registration.handler(simpleReplyHandler);
      return registration;
    }
    return null;
  }

  private <T> Handler<Message<T>> convertHandler(
      Handler<AsyncResult<Message<T>>> handler) {
    return reply -> {
      Future<Message<T>> result;
      if (reply.body() instanceof ReplyException) {
        ReplyException exception = (ReplyException) reply.body();
        result = Future.failedFuture(exception);
      } else {
        result = Future.succeededFuture(reply);
      }
      handler.handle(result);
    };
  }

  private String generateReplyAddress() {
    return Long.toString(replySequence.incrementAndGet());
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

  @SuppressWarnings("unchecked")
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

    private final MessageImpl message;
    final DeliveryOptions options;
    final HandlerRegistration handlerRegistration;
    final Iterator<Handler<SendContext>> iter = interceptors.iterator();

    SendContextImpl(MessageImpl message,
        DeliveryOptions options,
        HandlerRegistration handlerRegistration) {
      this.message = message;
      this.options = options;
      this.handlerRegistration = handlerRegistration;
    }

    @Override
    @SuppressWarnings("unchecked")
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
