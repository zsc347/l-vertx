package com.scaiz.vertx.net;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;

public interface NetClient {

  default NetClient connect(int port, String host,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    return connect(port, host, null, connectHandler);
  }

  NetClient connect(int port, String host, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler);

  default NetClient connect(SocketAddress remoteAddress,
      Handler<AsyncResult<NetSocket>> connectHandler) {
    return connect(remoteAddress, null, connectHandler);
  }

  NetClient connect(SocketAddress remoteAddress, String serverName,
      Handler<AsyncResult<NetSocket>> connectHandler);

  /**
   * close the client, any sockets which have not been
   */
  void close();
}
