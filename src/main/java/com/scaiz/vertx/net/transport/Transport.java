package com.scaiz.vertx.net.transport;

import com.scaiz.vertx.net.NetServerOptions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Transport {

  public static Transport JDK = new Transport();

  Transport() {

  }

  public static Transport nativeTransport() {
    Transport transport = null;
    try {
      Transport epoll = new EpollTransport();
      if (epoll.isAvailable()) {
        return epoll;
      } else {
        transport = epoll;
      }
    } catch (Throwable ignore) {
    }
    return transport;
  }

  public boolean isAvailable() {
    return true;
  }

  public Throwable unavailabilityCause() {
    return null;
  }

  public Class<? extends Channel> channelType(boolean domain) {
    if (domain) {
      throw new IllegalArgumentException();
    }
    return NioSocketChannel.class;
  }

  public Class<? extends ServerChannel> serverChannelType(boolean domain) {
    if(domain) {
      throw new IllegalArgumentException();
    }
    return NioServerSocketChannel.class;
  }

  public SocketAddress convert(com.scaiz.vertx.net.SocketAddress address,
      boolean resolved) {
    if (address.path() != null) {
      throw new IllegalStateException(
          "Domain socket not support by JDK transport");
    } else {
      if (resolved) {
        return new InetSocketAddress(address.host(), address.port());
      } else {
        return InetSocketAddress
            .createUnresolved(address.host(), address.port());
      }
    }
  }

  public void configure(NetServerOptions options, ServerBootstrap bootstrap) {
    // configure ServerBootstrap options, just skip it now
  }
}
