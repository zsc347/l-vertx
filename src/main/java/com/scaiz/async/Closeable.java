package com.scaiz.async;

public interface Closeable {

  void close(Handler<AsyncResult<Void>> completionHandler);
}
