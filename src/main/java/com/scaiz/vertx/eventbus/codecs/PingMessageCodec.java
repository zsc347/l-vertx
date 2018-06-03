package com.scaiz.vertx.eventbus.codecs;

import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.eventbus.MessageCodec;

public class PingMessageCodec implements MessageCodec<String, String> {

  @Override
  public void encodeToWire(Buffer buffer, String s) {

  }

  @Override
  public String decodeFromWire(int pos, Buffer buffer) {
    return null;
  }

  @Override
  public String transform(String s) {
    return null;
  }

  @Override
  public String name() {
    return "ping";
  }

  @Override
  public byte systemCodecID() {
    return 2;
  }
}
