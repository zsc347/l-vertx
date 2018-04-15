package com.scaiz.vertx.container;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.impl.TaskQueue;
import io.netty.channel.EventLoopGroup;

public interface VertxInternal extends Vertx {

  EventLoopGroup getEventLoopGroup();

  <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler,
      TaskQueue queue, Handler<AsyncResult<T>> resultHandler);
}
