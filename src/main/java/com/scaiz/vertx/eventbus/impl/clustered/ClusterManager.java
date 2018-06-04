package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.support.AsyncMultiMap;
import java.util.List;

public interface ClusterManager {

  <K, V> void getAsyncMultiMap(String name,
      Handler<AsyncResult<AsyncMultiMap<K, V>>> resultHandler);

  String getNodeID();

  void setVertx(Vertx vertx);

  void join(Handler<AsyncResult<Void>> resultHandler);

  void leave(Handler<AsyncResult<Void>> resultHandler);

  void nodeListener(NodeListener listener);

  List<String> getNodes();
}

