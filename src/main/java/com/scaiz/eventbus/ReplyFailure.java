package com.scaiz.eventbus;

public enum ReplyFailure {
  TIMEOUT;

  public static ReplyFailure fromInt(int i) {
    switch (i) {
      case 0:
        return TIMEOUT;
      default:
        throw new IllegalStateException("Invalid index " + i);
    }
  }

  public int toInt(int i) {
    switch (this) {
      case TIMEOUT:
        return 0;
      default:
        throw new IllegalStateException("How did we get here ?");
    }
  }
}
