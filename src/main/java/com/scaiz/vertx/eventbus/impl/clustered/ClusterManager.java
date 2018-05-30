package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.support.AsyncMultiMap;

public interface ClusterManager {

  <K, V> void getAsyncMultiMap(String name,
      Handler<AsyncResult<AsyncMultiMap<K, V>>> resultHandler);
}

