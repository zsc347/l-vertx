package com.scaiz.buffer.impl;

import com.scaiz.buffer.Buffer;
import com.scaiz.buffer.BufferFactory;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

public class BufferFactoryImpl implements BufferFactory {

  @Override
  public Buffer buffer() {
    return new BufferImpl();
  }

  @Override
  public Buffer buffer(int initialSizeHint) {
    return new BufferImpl(initialSizeHint);
  }

  @Override
  public Buffer buffer(String string) {
    return new BufferImpl(string);
  }

  @Override
  public Buffer buffer(String string, String enc) {
    return new BufferImpl(string, Charset.forName(enc));
  }

  @Override
  public Buffer buffer(byte[] bytes) {
    return new BufferImpl(bytes);
  }

  @Override
  public Buffer buffer(ByteBuf byteBuf) {
    return new BufferImpl(byteBuf);
  }
}
