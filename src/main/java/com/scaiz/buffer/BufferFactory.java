package com.scaiz.buffer;

import io.netty.buffer.ByteBuf;

public interface BufferFactory {

  public Buffer buffer();

  public Buffer buffer(int initialSizeHint);

  public Buffer buffer(String string);

  public Buffer buffer(String string, String enc);

  public Buffer buffer(byte[] bytes);

  public Buffer buffer(ByteBuf byteBuf);
}
