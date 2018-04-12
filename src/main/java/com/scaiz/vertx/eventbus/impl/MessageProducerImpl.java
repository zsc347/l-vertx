package com.scaiz.vertx.eventbus.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.eventbus.DeliveryOptions;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.MessageConsumer;
import com.scaiz.vertx.eventbus.MessageProducer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class MessageProducerImpl<T> implements MessageProducer<T> {

  public static final String CREDIT_ADDRESS_HEADER_NAME = "__vertx.credit";

  private final Vertx vertx;
  private final EventBus bus;
  private final String address;
  private final Queue<T> pending = new ArrayDeque<>();
  private final MessageConsumer<Integer> creditConsumer;
  private final boolean send;

  private DeliveryOptions options;
  private int credits = DEFAULT_WRITE_QUEUE_MAX_SIZE;
  private int maxSize = DEFAULT_WRITE_QUEUE_MAX_SIZE;
  private Handler<Void> drainHandler;

  public MessageProducerImpl(Vertx vertx, String address,
      boolean send, DeliveryOptions options) {
    this.vertx = vertx;
    this.bus = vertx.eventBus();
    this.address = address;
    this.send = send;
    if (send) {
      String creditAddress = UUID.randomUUID().toString() + "-credit";
      creditConsumer = bus.consumer(creditAddress,
          msg -> doReceiveCredit(msg.body()));
      options.addHeader(CREDIT_ADDRESS_HEADER_NAME, creditAddress);
    } else {
      this.creditConsumer = null;
    }
    this.options = options;

  }

  private synchronized void doReceiveCredit(int credit) {
    credits += credit;
    while (credits > 0) {
      T data = pending.poll();
      if (data == null) {
        break;
      } else {
        credits--;
        bus.send(address, data, options);
      }
    }
    checkDrained();
  }


  @Override
  public <R> MessageProducer<T> send(T message,
      Handler<AsyncResult<Message<R>>> replyHandler) {
    doSend(message, replyHandler);
    return this;
  }

  private synchronized <R> void doSend(T data,
      Handler<AsyncResult<Message<R>>> replyHandler) {
    if (credits > 0) {
      credits--;
      bus.send(address, data, options, replyHandler);
    } else {
      pending.add(data);
    }
  }

  @Override
  public MessageProducer<T> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public MessageProducer<T> write(T data) {
    if (send) {
      doSend(data, null);
    } else {
      bus.publish(address, data, options);
    }
    return this;
  }

  @Override
  public synchronized MessageProducer<T> setWriteQueueMaxSize(int setMaxSize) {
    credits += setMaxSize - maxSize;
    maxSize = setMaxSize;
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return credits == 0;
  }

  @Override
  public synchronized MessageProducer<T> drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    if (handler != null) {
      checkDrained();
    }
    return this;
  }

  private void checkDrained() {
    Handler<Void> handler = this.drainHandler;
    if (handler != null && credits >= maxSize / 2) {
      this.drainHandler = null;
      vertx.runOnContext(v -> handler.handle(null));
    }
  }

  @Override
  public MessageProducer<T> deliveryOptions(DeliveryOptions options) {
    if (creditConsumer != null) {
      options = new DeliveryOptions(options.toJson());
      options.addHeader(CREDIT_ADDRESS_HEADER_NAME,
          this.options.getHeaders().get(CREDIT_ADDRESS_HEADER_NAME));
    }
    this.options = options;
    return this;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public void end() {
    close();
  }

  @Override
  public void close() {
    if (creditConsumer != null) {
      creditConsumer.unregister();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }
}
