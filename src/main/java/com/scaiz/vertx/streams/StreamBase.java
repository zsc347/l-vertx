package com.scaiz.vertx.streams;

import com.scaiz.vertx.async.Handler;

public interface StreamBase {

  StreamBase exceptionHandler(Handler<Throwable> handler);
}
