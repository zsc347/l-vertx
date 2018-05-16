package com.scaiz.vertx.net.transport;

import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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

  public Class<? extends Channel> serverChannelType(boolean domain) {
    if (domain) {
      return EpollServerDomainSocketChannel.class;
    } else {
      return EpollServerSocketChannel.class;
    }
  }
}
