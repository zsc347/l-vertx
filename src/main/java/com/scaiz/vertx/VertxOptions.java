package com.scaiz.vertx;

import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.impl.clustered.ClusterManager;
import com.scaiz.vertx.net.resolver.AddressResolverOptions;

public class VertxOptions {

  private ClusterManager clusterManager;

  public long getBlockedThreadCheckInterval() {
    return 20 * 1000000; // ns
  }

  public long getMaxEventLoopExecuteTime() {
    return 100; // ms
  }

  public int getEventLoopPoolSize() {
    return 10;
  }

  public int getWorkerPoolSize() {
    return 5;
  }

  public long getMaxWorkerExecuteTime() {
    return 60 * 1000;
  }

  public int getInternalBlockingPoolSize() {
    return 5;
  }

  public AddressResolverOptions getAddressResolverOptions() {
    return new AddressResolverOptions();
  }

  public EventBusOptions getEventBusOptions() {
    return new EventBusOptions();
  }

  public boolean isClustered() {
    return Boolean.parseBoolean(
        System.getProperty("vert.option.isCluster", "false"));
  }

  public boolean isHAEnabled() {
    return Boolean.parseBoolean(
        System.getProperty("vert.option.ha", "false"));
  }

  public ClusterManager getClusteredManger() {
    return clusterManager;
  }

  public VertxOptions setClusterManager(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
    return this;
  }
}
