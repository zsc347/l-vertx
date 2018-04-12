package com.scaiz.vertx.eventbus;

public enum ReplyFailure {
  TIMEOUT,
  NO_HANDLERS,
  RECIPIENT_FAILURE;

  public static ReplyFailure fromInt(int i) {
    switch (i) {
      case 0:
        return TIMEOUT;
      case 1:
        return NO_HANDLERS;
      case 2:
        return RECIPIENT_FAILURE;
      default:
        throw new IllegalStateException("Invalid index " + i);
    }
  }

  public int toInt(int i) {
    switch (this) {
      case TIMEOUT:
        return 0;
      case NO_HANDLERS:
        return 1;
      case RECIPIENT_FAILURE:
        return 2;
      default:
        throw new IllegalStateException("How did we get here ?");
    }
  }
}
