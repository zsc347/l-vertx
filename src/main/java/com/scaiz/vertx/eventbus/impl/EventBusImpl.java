package com.scaiz.vertx.eventbus.impl;

import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Closeable;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.DeliveryOptions;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.eventbus.HandlerHolder;
import com.scaiz.vertx.eventbus.Handlers;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.MessageConsumer;
import com.scaiz.vertx.eventbus.MessageProducer;
import com.scaiz.vertx.eventbus.ReplyException;
import com.scaiz.vertx.eventbus.ReplyFailure;
import com.scaiz.vertx.eventbus.SendContext;
import com.scaiz.vertx.support.MultiMap;
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
  protected final CodecManager codecManager = new CodecManager();

  protected final VertxInternal vertx;

  protected boolean started;

  public EventBusImpl(VertxInternal vertx) {
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
    HandlerHolder holder = removeLocalRegistration(address, registration);
    removeRegistration(holder, address, completionHandler);
  }


  protected <T> void removeRegistration(HandlerHolder handlerHolder,
      String address,
      Handler<AsyncResult<Void>> completionHandler) {
    callCompletionHandlerAsync(completionHandler);
  }

  protected void callCompletionHandlerAsync(
      Handler<AsyncResult<Void>> completionHandler) {
    if (completionHandler != null) {
      vertx.runOnContext(
          v -> completionHandler.handle(Future.succeededFuture()));
    }
  }


  @SuppressWarnings("unchecked")
  private <T> HandlerHolder removeLocalRegistration(String address,
      HandlerRegistration<T> handler) {
    Handlers handlers = handlerMap.get(address);
    HandlerHolder lastHolder = null;
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
              lastHolder = holder;
            }
            holder.getContext().removeCloseHook(
                new HandlerEntry<>(address, holder.getHandler()));
          }
          break;
        }
      }
    }
    return lastHolder;
  }

  public <T> void addRegistration(String address,
      HandlerRegistration<T> registration,
      boolean replyHandler, boolean localOnly) {
    Objects.requireNonNull(registration.getHandler(), "handler");
    boolean newAddress = addLocalRegistration(address, registration,
        replyHandler, localOnly);

    addRegistration(newAddress, address, replyHandler, localOnly,
        registration::setResult);
  }

  protected <T> void addRegistration(boolean newAddress, String address,
      boolean replyHandler, boolean localOnly,
      Handler<AsyncResult<Void>> completionHandler) {
    completionHandler.handle(Future.succeededFuture());
  }


  private <T> boolean addLocalRegistration(String address,
      HandlerRegistration<T> registration, boolean replyHandler,
      boolean localOnly) {
    Objects.requireNonNull(address, "address");
    Context context = Vertx.currentContext();
    boolean hasContext = context != null;
    if (!hasContext) {
      context = vertx.getOrCreateContext();
    }
    registration.setHandlerContext(context);

    HandlerHolder holder = new HandlerHolder<>(registration, replyHandler,
        localOnly, context);

    Handlers handlers = handlerMap.get(address);
    boolean newAddress = false;
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

  protected <T> void sendReply(SendContextImpl<T> sendContext,
      MessageImpl replierMessage) {
    sendOrPub(sendContext);
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

  protected boolean isMessageLocal(MessageImpl msg) {
    return true;
  }

  private void assertStarted() {
    if (!started) {
      throw new IllegalStateException("Event Bus is not started");
    }
  }

  protected MessageImpl createMessage(boolean isSend, String address,
      MultiMap headers,
      Object body, String codecName) {
    Objects.requireNonNull(address, "address");
    MessageCodec codec = codecManager.lookupCodec(body, codecName);
    @SuppressWarnings("unchecked")
    MessageImpl msg = new MessageImpl(address, null, headers, body,
        codec, isSend, this);
    return msg;
  }

  protected <T> void sendOrPub(SendContextImpl<T> sendContext) {
    deliverMessageLocally(sendContext);
  }

  protected <T> void deliverMessageLocally(SendContextImpl<T> sendContext) {
    if (!deliverMessageLocally(sendContext.message)) {
      if (sendContext.handlerRegistration != null) {
        sendContext.handlerRegistration.sendAsyncResultFailure(
            ReplyFailure.NO_HANDLERS,
            "No handlers for address " + sendContext.message.address());
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean deliverMessageLocally(MessageImpl msg) {
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


  protected class SendContextImpl<T> implements SendContext<T> {

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
