package com.scaiz.vertx.async;

public interface Closeable {

  void close(Handler<AsyncResult<Void>> completionHandler);
}
