package com.scaiz.streams;

import com.scaiz.async.Handler;

public interface StreamBase {

  StreamBase exceptionHandler(Handler<Throwable> handler);
}
