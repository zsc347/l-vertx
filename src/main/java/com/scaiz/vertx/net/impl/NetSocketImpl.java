package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.eventbus.Message;
import com.scaiz.vertx.eventbus.MessageConsumer;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.NetSocketInternal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;
import java.nio.charset.Charset;
import java.util.UUID;
import javax.net.ssl.SSLSession;

public class NetSocketImpl extends ConnectionBase implements NetSocketInternal {

  private static final Handler<Object> NULL_MSG_HANDLER = event -> {
    if (event instanceof ByteBuf) {
      ((ByteBuf) event).release();
    }
  };

  private final String writeHandlerId;
  private final MessageConsumer registration;

  private Handler<Object> messageHandler;
  private boolean paused;
  private Buffer pendingData;
  private Handler<Void> endHandler;
  private Handler<Void> drainHandler;

  protected NetSocketImpl(VertxInternal vertx,
      ChannelHandlerContext chctx,
      ContextImpl context) {
    super(vertx, chctx, context);

    this.writeHandlerId = UUID.randomUUID().toString();
    Handler<Message<Buffer>> writeHandler = msg -> write(msg.body());
    registration = vertx.eventBus().<Buffer>consumer(writeHandlerId)
        .handler(writeHandler);
  }

  @Override
  protected void handleInterestedOpsChanged() {
    checkContext();
    callDrainHandler();
  }

  private void callDrainHandler() {
    if (drainHandler != null && !writeQueueFull()) {
      drainHandler.handle(null);
    }
  }

  public NetSocketImpl closeHandler(Handler<Void> closeHandler) {
    return (NetSocketImpl) super.closeHandler(closeHandler);
  }

  public NetSocketImpl exceptionHandler(Handler<Throwable> exceptionHandler) {
    return (NetSocketImpl) super.exceptionHandler(exceptionHandler);
  }

  @Override
  public ChannelHandlerContext channelHandlerContext() {
    return chctx;
  }

  @Override
  public NetSocketInternal writeMessage(Object message) {
    super.writeToChannel(message);
    return this;
  }

  @Override
  public NetSocketInternal writeMessage(Object message,
      Handler<AsyncResult<Void>> handler) {
    ChannelPromise promise = chctx.newPromise();
    super.writeToChannel(message, promise);
    promise.addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(future.cause()));
      }
    });
    return this;
  }

  @Override
  public NetSocket write(Buffer data) {
    ByteBuf buf = data.getByteBuf();
    super.writeToChannel(buf);
    return this;
  }

  @Override
  public NetSocketInternal messageHandler(Handler<Object> messageHandler) {
    if (messageHandler != null) {
      this.messageHandler = messageHandler;
    } else {
      this.messageHandler = NULL_MSG_HANDLER;
    }
    return this;
  }


  @Override
  public NetSocket setWriteQueueMaxSize(int maxSize) {
    return null;
  }

  @Override
  public boolean writeQueueFull() {
    return isNotWritable();
  }

  @Override
  public NetSocket drainHandler(Handler<Void> drainHandler) {
    this.drainHandler = drainHandler;
    vertx.runOnContext(v -> callDrainHandler());
    return this;
  }

  @Override
  public NetSocket handler(Handler<Buffer> dataHandler) {
    if (dataHandler != null) {
      messageHandler(
          new DataMessageHandler(channelHandlerContext().alloc(), dataHandler));
    } else {
      messageHandler(null);
    }
    return this;
  }

  @Override
  public synchronized NetSocket pause() {
    if (!paused) {
      paused = true;
      doPause();
    }
    return this;
  }

  @Override
  public synchronized NetSocket resume() {
    if (paused) {
      paused = false;
      if (pendingData != null) {
        context.runOnContext(v -> handleMessageReceived(Unpooled.EMPTY_BUFFER));
      }
      doResume();
    }
    return this;
  }

  private void handleMessageReceived(Object message) {
    checkContext();
    if (messageHandler != null) {
      messageHandler.handle(message);
    }
  }

  @Override
  public NetSocket endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerId;
  }

  @Override
  public NetSocket write(String str) {
    super.writeToChannel(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    return this;
  }

  @Override
  public NetSocket write(String str, String enc) {
    super.writeToChannel(Unpooled.copiedBuffer(str, Charset.forName(enc)));
    return this;
  }

  @Override
  public void end() {
    close();
  }

  @Override
  public synchronized void close() {
    chctx.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    chctx.flush();
  }

  @Override
  public NetSocket upgradeToSsl(String serverName, Handler<Void> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSsl() {
    return false;
  }

  @Override
  public SSLSession sslSession() {
    throw new UnsupportedOperationException("Not implemented");
  }


  @Override
  public synchronized void handleClosed() {
    checkContext();
    if (endHandler != null) {
      endHandler.handle(null);
    }
    super.handleClosed();
    if (vertx.eventBus() != null) {
      registration.unregister();
    }
  }


  @Override
  public String indicatedServerName() {
    throw new UnsupportedOperationException("Not implemented");
  }

  private class DataMessageHandler implements Handler<Object> {

    private final ByteBufAllocator allocator;
    private final Handler<Buffer> dataHandler;

    DataMessageHandler(
        ByteBufAllocator alloc, Handler<Buffer> dataHandler) {
      this.allocator = alloc;
      this.dataHandler = dataHandler;
    }

    @Override
    public void handle(Object event) {
      if (event instanceof ByteBuf) {
        ByteBuf byteBuf = (ByteBuf) event;
        byteBuf = ByteBufUtil.safeBuffer(byteBuf, allocator);
        Buffer data = Buffer.buffer(byteBuf);
        if (paused) {
          if (pendingData == null) {
            pendingData = data.copy();
          } else {
            pendingData.appendBuffer(data);
          }
        } else {
          if (pendingData != null) {
            data = pendingData.appendBuffer(data);
            pendingData = null;
          }
          dataHandler.handle(data);
        }
      }
    }
  }
}
