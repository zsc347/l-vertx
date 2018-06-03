package com.scaiz.vertx.eventbus.impl;

import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.codecs.IntCodec;
import com.scaiz.vertx.eventbus.codecs.PingMessageCodec;
import com.scaiz.vertx.eventbus.codecs.StringCodec;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CodecManager {

  public static final MessageCodec<String, String> PING_MESSAGE_CODEC =
      new PingMessageCodec();

  private static final MessageCodec<String, String>
      STRING_MESSAGE_CODEC = new StringCodec();
  private static final MessageCodec<Integer, Integer>
      INTEGER_MESSAGE_CODEC = new IntCodec();

  private final ConcurrentMap<String, MessageCodec>
      userCodecMap = new ConcurrentHashMap<>();

  private final MessageCodec[] systemCodes = new MessageCodec[]{
      STRING_MESSAGE_CODEC,
      INTEGER_MESSAGE_CODEC,
      PING_MESSAGE_CODEC
  };

  public MessageCodec lookupCodec(Object body, String codecName) {
    if (codecName != null) {
      MessageCodec codec = userCodecMap.get(codecName);
      if (codec == null) {
        throw new IllegalArgumentException(
            "No message codec for name: " + codecName);
      }
    } else if (body instanceof Integer) {
      return INTEGER_MESSAGE_CODEC;
    } else if (body instanceof String) {
      return STRING_MESSAGE_CODEC;
    }
    throw new IllegalArgumentException(
        "No message codec for type: " + body.getClass());
  }

  public void registerCodec(MessageCodec codec) {
    Objects.requireNonNull(codec, "codec");
    Objects.requireNonNull(codec.name(), "codec.name()");
    if (userCodecMap.putIfAbsent(codec.name(), codec) != null) {
      throw new IllegalStateException(
          "Already a codec registered with name :" + codec.name());
    }
  }

  public MessageCodec[] getSystemCodes() {
    return systemCodes;
  }

  public void unregisterCodec(String codecName) {
    userCodecMap.remove(codecName);
  }

  public <V, U> MessageCodec<U, V> getCodec(String codecName) {
    return userCodecMap.get(codecName);
  }
}
