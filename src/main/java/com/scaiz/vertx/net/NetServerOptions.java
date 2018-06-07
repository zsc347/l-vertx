package com.scaiz.vertx.net;

import com.scaiz.vertx.json.JsonObject;

public class NetServerOptions {

  private int port;

  public void setPort(int port) {
    this.port = port;
  }

  public NetServerOptions() {

  }

  public NetServerOptions(NetServerOptions options) {
    this.port = options.port;
  }

  public NetServerOptions(JsonObject json) {

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
