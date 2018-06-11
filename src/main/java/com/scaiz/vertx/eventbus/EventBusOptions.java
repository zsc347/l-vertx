package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.json.JsonObject;

public class EventBusOptions {

  private int port;

  public EventBusOptions() {
    port = 8086;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public String getClusterPublicHost() {
    return "127.0.0.1";
  }

  public int getClusterPublicPort() {
    return port;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("port", port);
    return json;
  }

  public long getClusterPingInterval() {
    return 1000;
  }
}
