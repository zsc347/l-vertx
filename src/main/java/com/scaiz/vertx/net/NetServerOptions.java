package com.scaiz.vertx.net;

public class NetServerOptions {

  public NetServerOptions(NetServerOptions options) {

  }

  public int getIdleTimeout() {
    return 1000;
  }

  public int getPort() {
    return 8086;
  }

  public String getHost() {
    return "127.0.0.1";
  }
}
