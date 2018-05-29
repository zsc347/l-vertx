package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;

public class ClusteredEventBus extends EventBusImpl {

  private EventBusOptions options;

  private final ClusterManager clusterManager;
  private final HAManager haManager;


  public ClusteredEventBus(VertxInternal vertx, VertxOptions vertxOptions,
      ClusterManager clusterManager, HAManager haManager) {
    super(vertx);
    this.options = vertxOptions.getEventBusOptions();
    this.clusterManager = clusterManager;
    this.haManager = haManager;
  }
}
