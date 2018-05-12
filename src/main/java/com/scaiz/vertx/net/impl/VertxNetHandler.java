package com.scaiz.vertx.net.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class VertxNetHandler implements ChannelHandler {

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {

  }

  public NetSocketImpl getConnection() {
    return null;
  }
}
