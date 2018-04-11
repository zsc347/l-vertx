package com.scaiz.eventbus.impl;

import com.scaiz.eventbus.MessageCodec;
import com.scaiz.eventbus.codecs.StringCodec;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CodecManager {

  public static final MessageCodec<String, String>
      STRING_MESSAGE_CODEC = new StringCodec();
  private final ConcurrentMap<String, MessageCodec>
      userCodecMap = new ConcurrentHashMap<>();

  public final MessageCodec[] systemCodes = new MessageCodec[]{
      STRING_MESSAGE_CODEC
  };

  public MessageCodec lookupCodec(Object body, String codecName) {
    if (codecName != null) {
      MessageCodec codec = userCodecMap.get(codecName);
      if (codec == null) {
        throw new IllegalArgumentException(
            "No message codec for name: " + codecName);
      }
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

  public void unregisterCodec(String codecName) {
    userCodecMap.remove(codecName);
  }
}
