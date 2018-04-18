package com.scaiz.vertx;

public class VertxException extends RuntimeException {

  public VertxException(String message) {
    super(message);
  }

  public VertxException(String message, Throwable cause) {
    super(message, cause);
  }

  public VertxException(Throwable cause) {
    super(cause);
  }
}
