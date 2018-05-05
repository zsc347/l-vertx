package com.scaiz.vertx.net;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.streams.ReadStream;

public interface NetServer {

  ReadStream<NetSocket> connectStream();

  NetServer connectHandler(Handler<NetSocket> handler);

  Handler<NetSocket> connectHandler();

  NetServer listen(Handler<AsyncResult<NetServer>> listenHandler);

  default NetServer listen() {
    listen((Handler<AsyncResult<NetServer>>) null);
    return this;
  }


  NetServer listen(int port, String host,
      Handler<AsyncResult<NetServer>> listenHandler);

  default NetServer listen(int port, String host) {
    listen(port, host, null);
    return this;
  }

  NetServer listen(int port, Handler<AsyncResult<NetServer>> listenHandler);

  default NetServer listen(int port) {
    listen(port, (Handler<AsyncResult<NetServer>>) null);
    return this;
  }

  NetServer listen(SocketAddress localAddress);

  NetServer listen(SocketAddress localAddress,
      Handler<AsyncResult<NetServer>> listenHandler);

  NetServer exceptionHandler(Handler<Throwable> exceptionHandler);

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

  int actualPort();



}
