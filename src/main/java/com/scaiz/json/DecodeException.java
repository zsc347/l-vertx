package com.scaiz.json;

public class DecodeException extends RuntimeException {

  public DecodeException(String message) {
    super(message);
  }

  public DecodeException(String message, Exception e) {
    super(message, e);
  }
}
