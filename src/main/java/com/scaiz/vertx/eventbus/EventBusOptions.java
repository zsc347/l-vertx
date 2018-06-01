package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.net.NetServerOptions;

public class EventBusOptions {

  public String getClusterPublicHost() {
    return "127.0.0.1";
  }

  public int getClusterPublicPort() {
    return 8089;
  }

  public NetServerOptions toJson() {
    return null;
  }
}
