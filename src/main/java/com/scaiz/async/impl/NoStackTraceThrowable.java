package com.scaiz.async.impl;

public class NoStackTraceThrowable extends Throwable {

  public NoStackTraceThrowable(String message) {
    super(message, null, false, false);
  }
}
