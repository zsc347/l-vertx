package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.net.SocketAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

class AsyncResolveConnectHelper {
  private List<Handler<AsyncResult<Channel>>> handlers = new ArrayList<>();
  private AsyncResult<Channel> result;
  private ChannelFuture future;


  public synchronized void addListener(Handler<AsyncResult<Channel>> handler) {
    if(result != null) {
      if (future != null) {
        future.addListener(v -> handler.handle(result));
      } else {
        handler.handle(result);
      }
    } else {
      handlers.add(handler);
    }
  }

  private synchronized void handle(ChannelFuture cf, AsyncResult<Channel> res) {
      if(result != null) {
        for(Handler<AsyncResult<Channel>> handler : handlers) {
          handler.handle(res);
        }
        future = cf;
        result = res;
      } else {
        throw new IllegalStateException("Already complete ...");
      }
  }

  private static void checkPort(int port) {
    if(port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port " + port);
    }
  }

  static AsyncResolveConnectHelper doBind(VertxInternal vertx,
      SocketAddress socketAddress, ServerBootstrap bootstrap) {
      AsyncResolveConnectHelper asyncResolveConnectHelper = new AsyncResolveConnectHelper();
      bootstrap.channel(vertx.transport().serverChannelType(socketAddress.path() != null));
      if(socketAddress.path() != null) {
        java.net.SocketAddress converted = vertx.transport().convert(socketAddress, true);
        ChannelFuture future = bootstrap.bind(converted);
        future.addListener(f -> {
          if(f.isSuccess()) {
            asyncResolveConnectHelper.handle(future, Future.succeededFuture());
          } else {
            asyncResolveConnectHelper.handle(future,Future.failedFuture(f.cause()));
          }
        });
      } else {
        checkPort(socketAddress.port());
        vertx.resolveAddress(socketAddress.host(), res -> {
          if(res.succeeded()) {
            InetSocketAddress t = new InetSocketAddress(socketAddress.port());
            ChannelFuture future = bootstrap.bind(t);
            future.addListener(f -> {
              if(f.isSuccess()) {
                asyncResolveConnectHelper.handle(future,
                    Future.succeededFuture(future.channel()));
              } else {
                asyncResolveConnectHelper.handle(future, Future.failedFuture(f.cause()));
              }
            });
          } else {
            asyncResolveConnectHelper.handle(null, Future.failedFuture(res.cause()));
          }
        });
      }
      return asyncResolveConnectHelper;
  }
}
