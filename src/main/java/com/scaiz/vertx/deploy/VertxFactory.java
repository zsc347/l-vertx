package com.scaiz.vertx.deploy;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;

public interface VertxFactory {

  Vertx vertx();

  Vertx vertx(VertxOptions options);

  void clusteredVertx(VertxOptions options, Handler<AsyncResult<Vertx>> resultHandler);
}
