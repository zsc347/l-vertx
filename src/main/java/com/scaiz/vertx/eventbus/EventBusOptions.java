package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.json.JsonObject;

public class EventBusOptions {

  public String getClusterPublicHost() {
    return "127.0.0.1";
  }

  public int getClusterPublicPort() {
    return 8089;
  }

  public JsonObject toJson() {
    return null;
  }

  public long getClusterPingInterval() {
    return 1000;
  }
}
