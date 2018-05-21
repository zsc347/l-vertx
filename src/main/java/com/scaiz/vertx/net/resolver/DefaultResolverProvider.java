package com.scaiz.vertx.net.resolver;

import com.scaiz.vertx.async.Handler;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import java.net.InetSocketAddress;

public class DefaultResolverProvider implements ResolverProvider {

  @Override
  public AddressResolverGroup<InetSocketAddress> resolver(
      AddressResolverOptions options) {
    return DefaultAddressResolverGroup.INSTANCE;
  }

  @Override
  public void close(Handler<Void> doneHandler) {
    doneHandler.handle(null);
  }
}
