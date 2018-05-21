package com.scaiz.vertx.net.resolver;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.impl.VertxImpl;
import io.netty.resolver.AddressResolverGroup;
import java.net.InetSocketAddress;

public interface ResolverProvider {

  static ResolverProvider factory(VertxImpl vertx,
      AddressResolverOptions options) {
    return new DefaultResolverProvider();
  }

  AddressResolverGroup<InetSocketAddress> resolver(
      AddressResolverOptions options);

  void close(Handler<Void> doneHandler);
}
