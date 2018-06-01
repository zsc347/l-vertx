package com.scaiz.vertx.net;

import com.scaiz.vertx.json.JsonObject;

public class NetServerOptions {

  public NetServerOptions() {

  }

  public NetServerOptions(NetServerOptions options) {

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
