package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.ContextTask;
import com.scaiz.vertx.container.impl.ContextImpl;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public abstract class VertxHandler<C extends ConnectionBase> extends
    ChannelDuplexHandler {

  private C conn;
  private ContextTask endReadAndFlush;
  private Handler<C> addHandler;
  private Handler<C> removeHandler;

  protected void setConnection(C connection) {
    conn = connection;
    endReadAndFlush = connection::endReadAndFlush;
    if (addHandler != null) {
      addHandler.handle(connection);
    }
  }

  protected C getConnection() {
    return conn;
  }


  public VertxHandler<C> setAddHandler(Handler<C> addHandler) {
    this.addHandler = addHandler;
    return this;
  }

  public VertxHandler<C> setRemoveHandler(Handler<C> removeHandler) {
    this.removeHandler = removeHandler;
    return this;
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx)
      throws Exception {
    C conn = getConnection();
    ContextImpl context = conn.getContext();
    context.executeFromIO(conn::handleInterestedOpsChanged);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext chctx, Throwable t)
      throws Exception {
    Channel ch = chctx.channel();
    C conn = getConnection();
    if (conn != null) {
      ContextImpl context = conn.getContext();
      context.executeFromIO(() -> {
        try {
          if (ch.isOpen()) {
            ch.close();
          }
        } catch (Exception ignore) {

        }
        conn.handlerException(t);
      });
    } else {
      ch.close();
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext chctx) throws Exception {
    if (removeHandler != null) {
      removeHandler.handle(conn);
    }
    ContextImpl context = conn.getContext();
    context.executeFromIO(conn::handleClosed);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext chctx)
      throws Exception {
    ContextImpl context = conn.getContext();
    context.executeFromIO(endReadAndFlush);
  }

  @Override
  public void channelRead(ChannelHandlerContext chctx, Object msg)
      throws Exception {
    Object message = decode(msg, chctx.alloc());
    ContextImpl context = conn.getContext();
    context.executeFromIO(() -> {
      conn.startRead();
      handleMessage(conn, context, chctx, message);
    });
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
      throws Exception {
    if (evt instanceof IdleStateEvent
        && ((IdleStateEvent) evt).state().equals(IdleState.ALL_IDLE)) {
      ctx.close();
    }
    ctx.fireUserEventTriggered(evt);
  }

  

  protected abstract void handleMessage(C conn, ContextImpl context,
      ChannelHandlerContext chctx,
      Object message);

  protected abstract Object decode(Object msg, ByteBufAllocator alloc);
}
