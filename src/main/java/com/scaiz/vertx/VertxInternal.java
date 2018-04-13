package com.scaiz.vertx;

import io.netty.channel.EventLoopGroup;

public interface VertxInternal extends Vertx {

  EventLoopGroup getEventLoopGroup();
}
