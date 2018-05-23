package com.scaiz.vertx;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.Context;
import com.scaiz.vertx.container.VertxThread;
import com.scaiz.vertx.eventbus.EventBus;
import com.scaiz.vertx.net.NetClient;
import com.scaiz.vertx.net.NetClientOptions;
import com.scaiz.vertx.net.NetServer;
import com.scaiz.vertx.net.NetServerOptions;

public interface Vertx {

  static Context currentContext() {
    Thread current = Thread.currentThread();
    if (current instanceof VertxThread) {
      return ((VertxThread) current).getContext();
    }
    return null;
  }

  void runOnContext(Handler<Void> action);

  void cancelTimer(long timeoutID);

  long setTimer(long delay, Handler<Long> handler);

  EventBus eventBus();

  Handler<Throwable> exceptionHandler();

  NetServer createNetServer(NetServerOptions options);

  default NetServer createNetServer() {
    return createNetServer(new NetServerOptions());
  }

  NetClient createNetClient(NetClientOptions options);

  default NetClient createNetClient() {
    return createNetClient(new NetClientOptions());
  }
}
