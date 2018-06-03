package com.scaiz.vertx.net;

import com.scaiz.vertx.json.JsonObject;

public class NetClientOptions {

  public NetClientOptions() {

  }

  public NetClientOptions(NetClientOptions options) {

  }

  public NetClientOptions(JsonObject entries) {

  }

  public int getIdleTimeout() {
    return 5000;
  }

  public int getReconnectAttempts() {
    return 3;
  }

  public ProxyOptions getProxyOption() {
    return null;
  }

  public int getReconnectInterval() {
    return 2;
  }
}
