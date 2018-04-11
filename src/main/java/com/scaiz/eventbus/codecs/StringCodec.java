package com.scaiz.eventbus.codecs;

import com.scaiz.buffer.Buffer;
import com.scaiz.eventbus.MessageCodec;
import java.nio.charset.StandardCharsets;

public class StringCodec implements MessageCodec<String, String> {

  @Override
  public void encodeToWire(Buffer buffer, String s) {
    byte[] strBytes = s.getBytes(StandardCharsets.UTF_8);
    buffer.appendInt(strBytes.length);
    buffer.appendBytes(strBytes);
  }

  @Override
  public String decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    byte[] strBytes = buffer.getBytes(pos, pos + length);
    return new String(strBytes, StandardCharsets.UTF_8);
  }

  @Override
  public String transform(String s) {
    return s;
  }

  @Override
  public String name() {
    return "string";
  }
}
