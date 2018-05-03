package com.scaiz.vertx.net;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.streams.ReadStream;
import com.scaiz.vertx.streams.WriteStream;
import javax.net.ssl.SSLSession;

public interface NetSocket extends ReadStream<Buffer>, WriteStream<Buffer> {

  NetSocket exceptionHandler(Handler<Throwable> handler);

  @Override
  NetSocket write(Buffer data);

  @Override
  NetSocket setWriteQueueMaxSize(int maxSize);

  @Override
  NetSocket drainHandler(Handler<Void> handler);

  @Override
  NetSocket handler(Handler<Buffer> handler);

  @Override
  NetSocket pause();

  @Override
  NetSocket resume();

  @Override
  NetSocket endHandler(Handler<Void> endHandler);

  String writeHandlerID();

  NetSocket write(String str);

  NetSocket write(String str, String enc);

  SocketAddress remoteAddres();

  SocketAddress localAddress();

  void end();

  void close();

  NetSocket closeHandler(Handler<Void> handler);

  NetSocket upgradeToSsl(String serverName, Handler<Void> handler);

  boolean isSsl();

  SSLSession sslSession();

  String indicatedServerName();
}
