package com.scaiz.vertx.net.transport;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public class EpollTransport extends Transport {

  EpollTransport() {

  }

  @Override
  public SocketAddress convert(com.scaiz.vertx.net.SocketAddress address,
      boolean resolved) {
    if (address.path() != null) {
      return new DomainSocketAddress(address.path());
    } else {
      if (resolved) {
        return new InetSocketAddress(address.host(), address.port());
      } else {
        return InetSocketAddress
            .createUnresolved(address.host(), address.port());
      }
    }
  }

  public EventLoopGroup eventLoopGroup(int nThreads,
      ThreadFactory threadFactory, int ioRatio) {
    EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(nThreads,
        threadFactory);
    epollEventLoopGroup.setIoRatio(ioRatio);
    return epollEventLoopGroup;
  }

  @Override
  public boolean isAvailable() {
    return Epoll.isAvailable();
  }

  @Override
  public Throwable unavailabilityCause() {
    return Epoll.unavailabilityCause();
  }

  public Class<? extends Channel> channelType(boolean domain) {
    if (domain) {
      return EpollDomainSocketChannel.class;
    } else {
      return EpollSocketChannel.class;
    }
  }

  public Class<? extends ServerChannel> serverChannelType(boolean domain) {
    if (domain) {
      return EpollServerDomainSocketChannel.class;
    } else {
      return EpollServerSocketChannel.class;
    }
  }
}
