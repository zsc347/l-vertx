package com.scaiz.vertx.eventbus.impl.clustered;

public interface NodeListener {

  void nodeAdded(String nodeID);

  void nodeLeft(String nodeID);
}
