package com.scaiz.vertx.net.impl;

import java.util.Objects;

public class ServerID {

  private int port;
  private String host;

  public ServerID(int port, String host) {
    this.port = port;
    this.host = host;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerID that = (ServerID) o;
    return this.port == that.port && Objects.equals(this.host, that.host);
  }

  @Override
  public int hashCode() {
    int result = port;
    result = 31 * result + (host == null ? 0 : host.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return host + ":" + port;
  }
}
