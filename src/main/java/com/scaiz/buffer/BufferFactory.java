package com.scaiz.buffer;

import io.netty.buffer.ByteBuf;

public interface BufferFactory {

  Buffer buffer();

  Buffer buffer(int initialSizeHint);

  Buffer buffer(String string);

  Buffer buffer(String string, String enc);

  Buffer buffer(byte[] bytes);

  Buffer buffer(ByteBuf byteBuf);
}
