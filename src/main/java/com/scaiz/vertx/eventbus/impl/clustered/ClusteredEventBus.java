package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.support.AsyncMultiMap;

public class ClusteredEventBus extends EventBusImpl {

  private EventBusOptions options;

  private final ClusterManager clusterManager;
  private final HAManager haManager;

  private static final String SUBS_MAP_NAME = "__vertx.subs";

  private AsyncMultiMap<String, ClusterNodeInfo> subs;
  private NetServer server;


  public ClusteredEventBus(VertxInternal vertx, VertxOptions vertxOptions,
      ClusterManager clusterManager, HAManager haManager) {
    super(vertx);
    this.options = vertxOptions.getEventBusOptions();
    this.clusterManager = clusterManager;
    this.haManager = haManager;
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    clusterManager.<String, ClusterNodeInfo>getAsyncMultiMap(SUBS_MAP_NAME,
        ar2 -> {
          if (ar2.succeeded()) {
            subs = ar2.result();
            server = vertx.createNetServer();
            server.connectHandler(getServerHandler());


          } else {

          }
        });
  }

  private Handler<NetSocket> getServerHandler() {
    return socket -> {

    };
  }
}
