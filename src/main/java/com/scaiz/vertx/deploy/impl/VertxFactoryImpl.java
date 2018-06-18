package com.scaiz.vertx.deploy.impl;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.deploy.VertxFactory;
import com.scaiz.vertx.impl.VertxImpl;

public class VertxFactoryImpl implements VertxFactory {

  @Override
  public Vertx vertx() {
    return new VertxImpl();
  }

  @Override
  public Vertx vertx(VertxOptions options) {
    if (options.isClustered()) {
      throw new IllegalArgumentException(
          "please user Vertx.clusteredVertx() to create a clustered vert.x instance");
    }
    return new VertxImpl(options, null);
  }

  @Override
  public void clusteredVertx(VertxOptions options,
      Handler<AsyncResult<Vertx>> resultHandler) {
    options.setClustered(true);
    new VertxImpl(options, resultHandler);
  }
}
