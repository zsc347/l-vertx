package com.scaiz.vertx.container.impl;

import java.util.concurrent.ExecutorService;

public class WorkerPool {

  private final ExecutorService pool;

  public WorkerPool(ExecutorService pool) {
    this.pool = pool;
  }

  ExecutorService executor() {
    return pool;
  }

  public void close() {
    pool.shutdown();
  }
}
