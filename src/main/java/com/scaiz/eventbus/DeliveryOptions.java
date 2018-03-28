package com.scaiz.eventbus;

import com.scaiz.support.MultiMap;

public class DeliveryOptions {

  public static final long DEFAULT_TIMEOUT = 30 * 1000;

  private long timeout = DEFAULT_TIMEOUT;
  private String codecName;
  private MultiMap headers;



}
