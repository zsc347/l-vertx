package com.scaiz.vertx.net;

import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import io.netty.channel.ChannelHandlerContext;

public interface NetSocketInternal extends NetSocket {

  ChannelHandlerContext channelHandlerContext();

  NetSocketInternal writeMessage(Object message);

  NetSocketInternal wirteMessage(Object message,
      Handler<AsyncResult<Void>> handler);

  NetSocketInternal messageHandler(Handler<Object> handler);
}
