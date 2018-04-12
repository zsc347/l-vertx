package com.scaiz.vertx.eventbus;

public class ReplyException extends RuntimeException {
  private final ReplyFailure failureType;
  private final int failureCode;


  public ReplyException(ReplyFailure failure, int failureCode, String message) {
    super(message);
    this.failureType = failure;
    this.failureCode = failureCode;
  }

  public ReplyException(ReplyFailure failure, String message) {
    this(failure, -1, message);
  }

  public ReplyException(ReplyFailure failureType) {
    this(failureType, -1, null);
  }

  public int failureCode() {
    return failureCode;
  }

  @Override
  public String toString() {
    String message = getMessage();
    return "(" + failureType + "," + failureCode + ") " + (message != null ? message : "");
  }
}
