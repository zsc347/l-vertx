package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.net.ProxyOptions;
import com.scaiz.vertx.net.SocketAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;

public class ChannelProvider {

  public static final ChannelProvider INSTANCE = new ChannelProvider();

  protected ChannelProvider() {

  }

  public void connect(VertxInternal vertx,
      Bootstrap bootstrap,
      ProxyOptions options,
      SocketAddress remoteAddress,
      Handler<Channel> channelInitializer,
      Handler<AsyncResult<Channel>> channelHandler) {
    bootstrap.resolver(vertx.nettyAddressResolverGroup());
    bootstrap.handler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel ch) throws Exception {
        channelInitializer.handle(ch);
      }
    });

    ChannelFuture fut = bootstrap.connect(vertx.transport()
        .convert(remoteAddress, false));
    fut.addListener(res -> {
      if (res.isSuccess()) {
        channelHandler.handle(Future.succeededFuture(fut.channel()));
      } else {
        channelHandler.handle(Future.failedFuture(fut.cause()));
      }
    });
  }

}
