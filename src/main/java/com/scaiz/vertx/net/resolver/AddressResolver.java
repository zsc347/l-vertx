package com.scaiz.vertx.net.resolver;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.impl.VertxImpl;
import io.netty.resolver.AddressResolverGroup;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class AddressResolver {

  private final ResolverProvider provider;
  private final AddressResolverGroup<InetSocketAddress> resolverGroup;
  private final VertxImpl vertx;

  public AddressResolver(VertxImpl vertx, AddressResolverOptions options) {
    this.provider = ResolverProvider.factory(vertx, options);
    this.resolverGroup = provider.resolver(options);
    this.vertx = vertx;
  }

  public void resolveHostName(String hostname,
      Handler<AsyncResult<InetAddress>> resultHandler) {
    ContextImpl callback = vertx.getOrCreateContext();
    io.netty.resolver.AddressResolver<InetSocketAddress> resolver =
        resolverGroup.getResolver(callback.nettyEventLoop());
    io.netty.util.concurrent.Future<InetSocketAddress> fut =
        resolver.resolve(InetSocketAddress.createUnresolved(hostname, 0));
    fut.addListener(a -> {
      if (a.isSuccess()) {
        callback.runOnContext(v -> {
          InetSocketAddress address = fut.getNow();
          resultHandler.handle(Future.succeededFuture(address.getAddress()));
        });
      } else {
        resultHandler.handle(Future.failedFuture(a.cause()));
      }
    });
  }

  public AddressResolverGroup<InetSocketAddress> nettyAddressResolverGroup() {
    return resolverGroup;
  }
}
