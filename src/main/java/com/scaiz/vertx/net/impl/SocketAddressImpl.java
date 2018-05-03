package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.net.SocketAddress;
import java.util.Objects;

public class SocketAddressImpl implements SocketAddress {

  private final String hostAddress;
  private final int port;
  private final String path;

  public SocketAddressImpl(String path) {
    this.port = -1;
    this.hostAddress = null;
    this.path = path;
  }

  public SocketAddressImpl(int port, String host) {
    Objects.requireNonNull(host, "host must not null");
    if (host.isEmpty()) {
      throw new IllegalArgumentException("host must not empty");
    }
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("port must in range [0, 65535]");
    }
    this.port = port;
    this.hostAddress = host;
    this.path = null;
  }

  @Override
  public String host() {
    return this.hostAddress;
  }

  @Override
  public int port() {
    return this.port;
  }

  @Override
  public String path() {
    return this.path;
  }

  @Override
  public String toString() {
    if (this.path == null) {
      return hostAddress + ":" + port;
    } else {
      return this.path;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SocketAddressImpl that = (SocketAddressImpl) o;
    if (port != that.port) {
      return false;
    }
    if (this.hostAddress != null ? !that.hostAddress.equals(this.hostAddress)
        : that.hostAddress != null) {
      return false;
    }
    return path != null ? this.path.equals(that.path) : that.path == null;
  }

  @Override
  public int hashCode() {
    int result = 0;
    result += hostAddress == null ? 0 : hostAddress.hashCode();
    result = 31 * result + (path == null ? 0 : path.hashCode());
    result = 31 * result + port;
    return result;
  }
}
