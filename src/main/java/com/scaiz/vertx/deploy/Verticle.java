package com.scaiz.vertx.deploy;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.container.Context;

public interface Verticle {

  Vertx getVertx();

  void init(Vertx vertx, Context context);

  void start(Future<Void> startFuture) throws Exception;

  void stop(Future<Void> stopFuture) throws Exception;
}
