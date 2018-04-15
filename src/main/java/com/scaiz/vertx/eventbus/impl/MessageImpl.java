package com.scaiz.vertx.eventbus.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.eventbus.DeliveryOptions;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.ReplyException;
import com.scaiz.vertx.eventbus.ReplyFailure;
import com.scaiz.vertx.support.CaseInsensitiveHeaders;
import com.scaiz.vertx.support.MultiMap;

public class MessageImpl<U, V> implements Message<V> {

  private EventBusImpl bus;

  private MessageCodec<U, V> messageCodec;

  private String address;
  private String replyAddress;


  private MultiMap headers;
  private U sendBody;
  private V receiveBody;

  private boolean isSend;

  MessageImpl(String address, String replyAddress,
      MultiMap headers, U sendBody, MessageCodec codec, boolean isSend,
      EventBusImpl eventBus) {
    this.address = address;
    this.replyAddress = replyAddress;
    this.headers = headers;
    this.sendBody = sendBody;
    this.messageCodec = codec;
    this.isSend = isSend;
    this.bus = eventBus;
  }

  private MessageImpl(MessageImpl<U, V> other) {
    this.bus = other.bus;
    this.address = other.address;
    this.replyAddress = other.replyAddress;
    this.messageCodec = other.messageCodec;

    if (other.headers != null) {
      this.headers = new CaseInsensitiveHeaders();
      this.headers.addAll(other.headers);
    }

    if (other.sendBody != null) {
      this.sendBody = other.sendBody;
      this.receiveBody = messageCodec.transform(other.sendBody);
    }

    this.isSend = other.isSend;
  }

  U sendBody() {
    return sendBody;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String replyAddress() {
    return replyAddress;
  }

  @Override
  public V body() {
    if (receiveBody == null && sendBody != null) {
      receiveBody = messageCodec.transform(sendBody);
    }
    return receiveBody;
  }

  @Override
  public MultiMap headers() {
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
    return headers;
  }

  @Override
  public boolean isSend() {
    return isSend;
  }

  @Override
  public <R> void reply(Object message, DeliveryOptions options,
      Handler<AsyncResult<Message<R>>> replyHandler) {
    if (replyAddress != null) {
      sendReply(
          bus.createMessage(true, replyAddress, options.getHeaders(), message,
              options.getCodecName()), options, replyHandler);
    }
  }

  @Override
  public void fail(int failureCode, String message) {
    if (replyAddress != null) {
      sendReply(bus.createMessage(true, replyAddress, null,
          new ReplyException(ReplyFailure.RECIPIENT_FAILURE,
              failureCode, message), null), null, null);
    }
  }

  public void setBus(EventBusImpl bus) {
    this.bus = bus;
  }

  private <R> void sendReply(MessageImpl msg, DeliveryOptions options,
      Handler<AsyncResult<Message<R>>> replyHandler) {
    if (bus != null) {
      bus.sendReply(msg, options, replyHandler);
    }
  }

  public MessageImpl<U, V> copyBeforeReceive() {
    return new MessageImpl<>(this);
  }

  public void setReplyAddress(String replyAddress) {
    this.replyAddress = replyAddress;
  }
}