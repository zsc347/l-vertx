package com.scaiz.vertx.net;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.streams.ReadStream;

public interface NetServer {

  NetServer connectHandler(Handler<NetSocket> handler);

  Handler<NetSocket> connectHandler();

  NetServer listen(Handler<AsyncResult<NetServer>> listenHandler);

  default NetServer listen() {
    listen((Handler<AsyncResult<NetServer>>) null);
    return this;
  }

  NetServer listen(SocketAddress localAddress,
      Handler<AsyncResult<NetServer>> listenHandler);

  default NetServer listen(SocketAddress localAddress) {
    return listen(localAddress, null);
  }

  NetServer exceptionHandler(Handler<Throwable> exceptionHandler);

  default void close() {
    close(null);
  }

  void close(Handler<AsyncResult<Void>> completionHandler);

  int actualPort();
}
