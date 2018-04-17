package com.scaiz.vertx.container.impl;

import java.util.concurrent.ExecutorService;

class WorkerPool {

  private final ExecutorService pool;

  WorkerPool(ExecutorService pool) {
    this.pool = pool;
  }

  ExecutorService executor() {
    return pool;
  }

  void close() {
    pool.shutdown();
  }
}
