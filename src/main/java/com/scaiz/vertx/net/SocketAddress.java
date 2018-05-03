package com.scaiz.vertx.net;

import com.scaiz.vertx.net.impl.SocketAddressImpl;

public interface SocketAddress {

  static SocketAddress inetSocketAddress(int port, String host) {
    return new SocketAddressImpl(port, host);
  }

  static SocketAddress domainSocketAddress(String path) {
    return new SocketAddressImpl(path);
  }

  String host();

  int port();

  String path();
}
