package com.scaiz.vertx.net.impl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import java.util.function.Function;

public abstract class VertxNetHandler extends VertxHandler<NetSocketImpl> {

  private final Function<ChannelHandlerContext, NetSocketImpl>
      connectionFactory;

  public VertxNetHandler(Function<ChannelHandlerContext, NetSocketImpl>
      connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public VertxNetHandler(NetSocketImpl conn) {
    this(ctx -> conn);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    setConnection(connectionFactory.apply(ctx));
  }

  @Override
  protected Object decode(Object msg, ByteBufAllocator alloc) {
    return msg;
  }
}
