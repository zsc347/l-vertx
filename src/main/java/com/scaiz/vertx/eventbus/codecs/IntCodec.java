package com.scaiz.vertx.eventbus.codecs;

import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.eventbus.MessageCodec;

public class IntCodec implements MessageCodec<Integer, Integer> {

  @Override
  public void encodeToWire(Buffer buffer, Integer integer) {
    buffer.appendInt(integer);
  }

  @Override
  public Integer decodeFromWire(int pos, Buffer buffer) {
    return buffer.getInt(pos);
  }

  @Override
  public Integer transform(Integer integer) {
    return integer;
  }

  @Override
  public String name() {
    return "int";
  }

  @Override
  public byte systemCodecID() {
    return 1;
  }
}
