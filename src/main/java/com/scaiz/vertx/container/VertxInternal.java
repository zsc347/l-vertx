package com.scaiz.vertx.container;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.net.impl.NetServerImpl;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.net.transport.Transport;
import io.netty.channel.EventLoopGroup;
import io.netty.resolver.AddressResolverGroup;
import java.net.InetAddress;
import java.util.Map;

public interface VertxInternal extends Vertx {

  EventLoopGroup getEventLoopGroup();

  Map<ServerID, NetServerImpl> sharedNetServers();

  Transport transport();

  ContextImpl getContext();

  ContextImpl getOrCreateContext();

  void resolveAddress(String hostname,
      Handler<AsyncResult<InetAddress>> resultHandler);

  AddressResolverGroup<?> nettyAddressResolverGroup();
}
