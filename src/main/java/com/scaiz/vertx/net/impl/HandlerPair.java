package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.net.NetSocket;
import java.util.Objects;

public class HandlerPair {

  final Handler<NetSocket> connectionHandler;
  final Handler<Throwable> exceptionHandler;

  public HandlerPair(Handler<NetSocket> connectionHandler,
      Handler<Throwable> exceptionHandler) {
    this.connectionHandler = connectionHandler;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HandlerPair that = (HandlerPair) o;

    return Objects.equals(connectionHandler, that.connectionHandler)
        && Objects.equals(exceptionHandler, that.exceptionHandler);
  }

  @Override
  public int hashCode() {
    int result = 0;
    if (connectionHandler != null) {
      result = 31 * result + connectionHandler.hashCode();
    }
    if (exceptionHandler != null) {
      result = 31 * result + exceptionHandler.hashCode();
    }
    return result;
  }

}
