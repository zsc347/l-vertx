package com.scaiz.vertx.net;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;

public interface NetClient {

  NetClient connect(int port, String host,
      Handler<AsyncResult<NetSocket>> connectHandler);

  NetClient connect(int port, String host, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler);

  NetClient connect(SocketAddress remoteAddress,
      Handler<AsyncResult<NetSocket>> connectHandler);

  NetClient connect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler);

  /**
   * close the client, any sockets which have not been
   */
  void close();
}
