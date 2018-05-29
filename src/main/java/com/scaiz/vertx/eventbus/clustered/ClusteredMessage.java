package com.scaiz.vertx.eventbus.clustered;

import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.eventbus.impl.MessageImpl;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.support.CaseInsensitiveHeaders;
import com.scaiz.vertx.support.MultiMap;
import io.netty.util.CharsetUtil;
import java.util.List;
import java.util.Map.Entry;

public class ClusteredMessage<U, V> extends MessageImpl<U, V> {

  private ServerID sender;
  private Buffer wireBuffer;
  private int bodyPos;
  private int headerPos;
  private boolean fromWire;

  ClusteredMessage(String address, String replyAddress,
      MultiMap headers, U sendBody,
      MessageCodec codec, boolean isSend,
      EventBusImpl eventBus) {
    super(address, replyAddress, headers, sendBody, codec, isSend, eventBus);
  }

  protected ClusteredMessage(ClusteredMessage<U, V> other) {
    super(other);
    this.sender = other.sender;
    if (other.sendBody == null) {
      this.wireBuffer = other.wireBuffer;
      this.bodyPos = other.bodyPos;
      this.headerPos = other.headerPos;
    }
    this.fromWire = other.fromWire;
  }

  @Override
  public ClusteredMessage<U, V> copyBeforeReceive() {
    return new ClusteredMessage<>(this);
  }

  @Override
  public MultiMap headers() {
    if (headers == null) {
      if (headerPos != 0) {
        decodeHeaders();
      }
      if (headers == null) {
        headers = new CaseInsensitiveHeaders();
      }
    }
    return headers;
  }

  @Override
  public V body() {
    if (receiveBody == null && bodyPos != 0) {
      decodeBody();
    }
    return receiveBody;
  }

  private void writeString(Buffer buffer, String str) {
    byte[] bytes = str.getBytes(CharsetUtil.UTF_8);
    buffer.appendInt(bytes.length);
    buffer.appendBytes(bytes);
  }

  private void encodeHeaders(Buffer buffer) {
    if (headers != null && !headers.isEmpty()) {
      int headerStartPos = buffer.length();
      buffer.appendInt(0);
      buffer.appendInt(headers.size());
      List<Entry<String, String>> entries = headers.entries();
      for (Entry<String, String> entry : entries) {
        writeString(buffer, entry.getKey());
        writeString(buffer, entry.getValue());
      }
      int headersEndPos = buffer.length();
      buffer.setInt(headerStartPos, headersEndPos - headerStartPos);
    } else {
      buffer.appendInt(4);
    }
  }

  private void decodeHeaders() {
    int length = wireBuffer.getInt(headerPos);
    if (length != 4) {
      headerPos += 4;
      int numHeaders = wireBuffer.getInt(headerPos);
      headerPos += 4;
      headers = new CaseInsensitiveHeaders();
      for (int i = 0; i < numHeaders; i++) {
        int keyLength = wireBuffer.getInt(headerPos);
        headerPos += 4;
        byte[] bytes = wireBuffer.getBytes(headerPos, keyLength);
        String key = new String(bytes, CharsetUtil.UTF_8);
        headerPos += keyLength;

        int valLength = wireBuffer.getInt(headerPos);
        headerPos += 4;
        bytes = wireBuffer.getBytes(headerPos, valLength);
        String val = new String(bytes, CharsetUtil.UTF_8);
        headerPos += valLength;
        headers.add(key, val);
      }
    }
    headerPos = 0;
  }

  private void encodeBody(Buffer buffer) {
    messageCodec.encodeToWire(buffer, sendBody);
  }

  private void decodeBody() {
    receiveBody = messageCodec.decodeFromWire(bodyPos, wireBuffer);
    bodyPos = 0;
  }


  @Override
  public String replyAddress() {
    return replyAddress;
  }

  public boolean isFromWire() {
    return fromWire;
  }
}
