package com.scaiz.eventbus.impl;

import com.scaiz.async.AsyncResult;
import com.scaiz.async.Handler;
import com.scaiz.eventbus.Message;
import com.scaiz.eventbus.MessageConsumer;
import com.scaiz.streams.ReadStream;
import java.util.ArrayDeque;
import java.util.Queue;

public class HandlerRegistration<T> implements MessageConsumer<T> {


  private final int DEFAULT_MAX_BUFFERED_MESSAGES = 1000;

  private final Queue<Message<T>> pending = new ArrayDeque<>(8);

  private int maxBufferedMessages = DEFAULT_MAX_BUFFERED_MESSAGES;


  @Override
  public MessageConsumer<T> exceptionHandler(Handler<Throwable> handler) {
    return null;
  }

  @Override
  public MessageConsumer<T> handler(Handler<Message<T>> handler) {
    return null;
  }

  @Override
  public MessageConsumer<T> pause() {
    return null;
  }

  @Override
  public MessageConsumer<T> resume() {
    return null;
  }

  @Override
  public MessageConsumer<T> endHandler(Handler<Void> endHandler) {
    return null;
  }

  @Override
  public ReadStream<T> bodyStream() {
    return null;
  }

  @Override
  public boolean isRegistered() {
    return false;
  }

  @Override
  public String address() {
    return null;
  }

  @Override
  public synchronized MessageConsumer<T> setMaxBufferedMessages(
      int maxBufferedMessages) {
    if (maxBufferedMessages < 0) {
      throw new IllegalArgumentException("Max buffer messages must >= 0");
    }
    while (pending.size() > maxBufferedMessages) {
      pending.poll();
    }
    this.maxBufferedMessages = maxBufferedMessages;
    return this;
  }

  @Override
  public synchronized int getMaxBufferedMessages() {
    return maxBufferedMessages;
  }

  @Override
  public void completionHandler(Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public void unregister(Handler<AsyncResult<Void>> completionHandler) {

  }
}
