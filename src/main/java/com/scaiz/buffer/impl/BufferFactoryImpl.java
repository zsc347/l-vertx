package com.scaiz.buffer.impl;

import com.scaiz.buffer.Buffer;
import com.scaiz.buffer.BufferFactory;
import io.netty.buffer.ByteBuf;

public class BufferFactoryImpl implements BufferFactory {

  @Override
  public Buffer buffer() {
    return null;
  }

  @Override
  public Buffer buffer(int initialSizeHint) {
    return null;
  }

  @Override
  public Buffer buffer(String string) {
    return null;
  }

  @Override
  public Buffer buffer(String string, String enc) {
    return null;
  }

  @Override
  public Buffer buffer(byte[] bytes) {
    return null;
  }

  @Override
  public Buffer buffer(ByteBuf byteBuf) {
    return null;
  }
}
