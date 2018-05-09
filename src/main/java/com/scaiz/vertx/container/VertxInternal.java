package com.scaiz.vertx.container;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.net.impl.NetServerImpl;
import com.scaiz.vertx.net.impl.ServerID;
import io.netty.channel.EventLoopGroup;
import java.util.Map;

public interface VertxInternal extends Vertx {

  EventLoopGroup getEventLoopGroup();

  Map<ServerID, NetServerImpl> sharedNetServers();

  Context getContext();
}
