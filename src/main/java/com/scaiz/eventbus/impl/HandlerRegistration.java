package com.scaiz.eventbus.impl;

import com.scaiz.Context;
import com.scaiz.Vertx;
import com.scaiz.async.AsyncResult;
import com.scaiz.async.Future;
import com.scaiz.async.Handler;
import com.scaiz.eventbus.Message;
import com.scaiz.eventbus.MessageConsumer;
import com.scaiz.eventbus.ReplyException;
import com.scaiz.eventbus.ReplyFailure;
import com.scaiz.streams.ReadStream;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class HandlerRegistration<T> implements MessageConsumer<T>, Handler<Message<T>> {


  private final int DEFAULT_MAX_BUFFERED_MESSAGES = 1000;

  private final Vertx vertx;
  private final String address;
  private final String repliedAddress;
  private final EventBusImpl eventBus;
  private final Queue<Message<T>> pending = new ArrayDeque<>(8);

  private int maxBufferedMessages = DEFAULT_MAX_BUFFERED_MESSAGES;
  private AsyncResult<Void> result;
  private long timeoutID = -1;
  private boolean registered;
  private boolean paused;
  private Handler<AsyncResult<Void>> completionHandler;

  private Context handlerContext;
  private Handler<Message<T>> handler;
  private Handler<Message<T>> discardHandler;
  private Handler<Void> endHandler;


  public HandlerRegistration(Vertx vertx, String address, String repliedAddress,
                             AsyncResult<Void> result, EventBusImpl eventBus,
                             Handler<AsyncResult<Message<T>>> asyncResultHandler, long timeout) {
    this.vertx = vertx;
    this.address = address;
    this.repliedAddress = repliedAddress;
    this.result = result;
    this.eventBus = eventBus;

    if (timeout != -1) {
      timeoutID = vertx.setTimer(timeout, tid -> {
        unregister();
        asyncResultHandler.handle(Future.failedFuture(
          new ReplyException(ReplyFailure.TIMEOUT,
            "Timed out after waiting " + timeout
              + "(ms) for a reply. address: " + address
              + ", repliedAddress: " + repliedAddress)));
      });
    }
  }


  @Override
  public MessageConsumer<T> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public synchronized MessageConsumer<T> handler(Handler<Message<T>> handler) {
    this.handler = handler;
    if (this.handler != null && !registered) {
      registered = true;
      eventBus.addRegistration(address, this, repliedAddress != null, true);
    } else if (this.handler == null && registered) {
      this.unregister();
    }
    return this;
  }

  @Override
  public synchronized MessageConsumer<T> pause() {
    if (!paused) {
      paused = true;
    }
    return this;
  }

  @Override
  public synchronized MessageConsumer<T> resume() {
    if (paused) {
      paused = false;
    }
    checkNextTick();
    return this;
  }

  @Override
  public synchronized MessageConsumer<T> endHandler(Handler<Void> endHandler) {
    if (endHandler != null) {
      // TODO fix run on context
      this.endHandler = v1 -> vertx.runOnContext(v2 -> endHandler.handle(null));
    } else {
      this.endHandler = null;
    }
    return this;
  }

  @Override
  public ReadStream<T> bodyStream() {
    return null;
  }

  @Override
  public synchronized boolean isRegistered() {
    return registered;
  }

  @Override
  public String address() {
    return address;
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
    Objects.requireNonNull(completionHandler);
    if (result != null) {
      final AsyncResult<Void> theValue = result;
      vertx.runOnContext(v -> completionHandler.handle(theValue));
    } else {
      this.completionHandler = completionHandler;
    }
  }

  @Override
  public void unregister(Handler<AsyncResult<Void>> completionHandler) {
    doUnregister(completionHandler);
  }

  private synchronized void doUnregister(Handler<AsyncResult<Void>> completionHandler) {
    if (timeoutID != -1) {
      vertx.cancelTimer(timeoutID);
    }
    if (endHandler != null) {
      Handler<Void> theEndHandler = endHandler;
      Handler<AsyncResult<Void>> handler = completionHandler;
      completionHandler = ar -> {
        theEndHandler.handle(null);
        if (handler != null) {
          handler.handle(ar);
        }
      };
    }
    if (registered) {
      registered = false;
      eventBus.removeRegistration(address, this, completionHandler);
    } else {
      if (completionHandler != null) {
        final Handler<AsyncResult<Void>> theCompletionHandler = completionHandler;
        vertx.runOnContext(v -> theCompletionHandler.handle(Future.succeededFuture()));
      }
    }
  }

  @Override
  public void handle(Message<T> message) {
    Handler<Message<T>> theHandler = null;
    synchronized (this) {
      if (paused) {
        if (pending.size() < maxBufferedMessages) {
          pending.add(message);
        } else {
          if (discardHandler != null) {
            discardHandler.handle(message);
          }
        }
      } else {
        if (pending.size() > 0) {
          pending.add(message);
          message = pending.poll();
        }
        theHandler = handler;
      }
    }
    deliver(theHandler, message);
  }

  private void deliver(Handler<Message<T>> theHandler, Message<T> message) {
    checkNextTick();
    String creditsAddress = message.headers().get(MessageProducerImpl.CREDIT_ADDRESS_HEADER_NAME);
    if (creditsAddress != null) {
      eventBus.send(creditsAddress, 1);
    }
    try {
      theHandler.handle(message);
    } catch (Exception e) {
      throw e;
    }
  }

  private synchronized void checkNextTick() {
    if (!pending.isEmpty()) {
      handlerContext.runOnContext(v -> {
        Handler<Message<T>> theHandler;
        Message<T> message;
        synchronized (HandlerRegistration.this) {
          if (paused || (message = pending.poll()) == null) {
            return;
          }
          theHandler = handler;
        }
        deliver(theHandler, message);
      });
    }
  }

  synchronized void setHandlerContext(Context handlerContext) {
    this.handlerContext = handlerContext;
  }

  synchronized void setDiscardHandler(Handler<Message<T>> discardHandler) {
    this.discardHandler = discardHandler;
  }

  Handler<Message<T>> getHandler() {
    return handler;
  }
}
