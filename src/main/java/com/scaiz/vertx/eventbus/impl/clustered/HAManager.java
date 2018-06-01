package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.json.JsonObject;

public interface HAManager {

  void addDataToHAInfo(String serverIdHaKey, JsonObject put);
}
