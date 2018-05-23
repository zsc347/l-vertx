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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyConnectionEvent;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ProxyChannelProvider extends ChannelProvider {

  public static ProxyChannelProvider INSTANCE = new ProxyChannelProvider();

  private ProxyChannelProvider() {
  }

  @Override
  public void connect(VertxInternal vertx,
      Bootstrap bootstrap,
      ProxyOptions options,
      SocketAddress remoteAddress,
      Handler<Channel> channelInitializer,
      Handler<AsyncResult<Channel>> channelHandler) {

    final String proxyHost = options.getHost();
    final int proxyPort = options.getPort();
    final String proxyUserName = options.getUserName();
    final String proxyPassword = options.getPassword();
    final ProxyType proxyType = options.getType();

    vertx.resolveAddress(proxyHost, dnsRes -> {
      if (dnsRes.succeeded()) {
        InetAddress address = dnsRes.result();
        InetSocketAddress proxyAddr = new InetSocketAddress(address, proxyPort);

        ProxyHandler proxy;
        switch (proxyType) {
          case HTTP:
            proxy = proxyUserName != null
                ? new HttpProxyHandler(proxyAddr, proxyUserName, proxyPassword)
                : new HttpProxyHandler(proxyAddr);
            break;
          case SOCKS4:
            proxy = proxyUserName != null
                ? new Socks4ProxyHandler(proxyAddr, proxyUserName)
                : new Socks4ProxyHandler(proxyAddr);
            break;
          case SOCKS5:
            proxy = proxyUserName != null
                ? new Socks5ProxyHandler(proxyAddr, proxyUserName,
                proxyPassword)
                : new Socks5ProxyHandler(proxyAddr);
            break;
          default:
            throw new IllegalStateException("How did we get here ?");
        }

        bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);

        java.net.SocketAddress targetAddress = vertx.transport()
            .convert(remoteAddress, false);

        bootstrap.handler(new ChannelInitializer<Channel>() {
          @Override
          protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addFirst("proxy", proxy);

            pipeline.addLast(new ChannelInboundHandlerAdapter() {

              @Override
              public void userEventTriggered(ChannelHandlerContext chctx,
                  Object evt) throws Exception {
                if (evt instanceof ProxyConnectionEvent) {
                  pipeline.remove(proxy);
                  pipeline.remove(this);
                  channelInitializer.handle(ch);
                  channelHandler.handle(Future.succeededFuture(ch));
                }
                chctx.fireUserEventTriggered(evt);
              }

              @Override
              public void exceptionCaught(ChannelHandlerContext chctx,
                  Throwable cause) throws Exception {
                channelHandler.handle(Future.failedFuture(cause));
              }
            });
          }
        });

        ChannelFuture fut = bootstrap.connect(targetAddress);
        fut.addListener(res -> {
          if (!res.isSuccess()) {
            channelHandler.handle(Future.failedFuture(fut.cause()));
          }
        });
      } else {
        channelHandler.handle(Future.failedFuture(dnsRes.cause()));
      }
    });
  }
}
