package com.scaiz.vertx.container;

import com.scaiz.vertx.Vertx;
import io.netty.channel.EventLoopGroup;

public interface VertxInternal extends Vertx {

  EventLoopGroup getEventLoopGroup();
}
