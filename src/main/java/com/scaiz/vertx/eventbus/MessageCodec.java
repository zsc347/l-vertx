package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.buffer.Buffer;

/**
 * @param <S> send type S
 * @param <R> Receive type R
 */
public interface MessageCodec<S, R> {

  void encodeToWire(Buffer buffer, S s);

  R decodeFromWire(int pos, Buffer buffer);

  R transform(S s);

  String name();

  byte systemCodecID();
}
