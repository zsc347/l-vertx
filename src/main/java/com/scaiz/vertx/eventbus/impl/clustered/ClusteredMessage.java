package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.eventbus.MessageCodec;
import com.scaiz.vertx.eventbus.impl.CodecManager;
import com.scaiz.vertx.eventbus.impl.EventBusImpl;
import com.scaiz.vertx.eventbus.impl.MessageImpl;
import com.scaiz.vertx.net.impl.ServerID;
import com.scaiz.vertx.support.CaseInsensitiveHeaders;
import com.scaiz.vertx.support.MultiMap;
import com.sun.security.ntlm.Server;
import io.netty.util.CharsetUtil;
import java.util.List;
import java.util.Map.Entry;

public class ClusteredMessage<U, V> extends MessageImpl<U, V> {

  private static final byte WIRE_PROTOCOL_VERSION = 1;
  private ServerID sender;
  private Buffer wireBuffer;
  private int bodyPos;
  private int headerPos;
  private boolean fromWire;

  public ClusteredMessage() {

  }

  ClusteredMessage(ServerID sender, String address, String replyAddress,
      MultiMap headers, U sendBody,
      MessageCodec codec, boolean isSend,
      EventBusImpl eventBus) {
    super(address, replyAddress, headers, sendBody, codec, isSend, eventBus);
    this.sender = sender;
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


  public Buffer encodeToWire() {
    int length = 1024;
    Buffer buffer = Buffer.buffer(length);
    buffer.appendInt(0); // place holder for overall length

    buffer.appendByte(WIRE_PROTOCOL_VERSION);
    byte systemCodecID = messageCodec.systemCodecID();
    buffer.appendByte(systemCodecID);
    if (systemCodecID == -1) {
      writeString(buffer, messageCodec.name());
    }

    buffer.appendByte(send ? (byte) 0 : (byte) 1);

    writeString(buffer, address);

    if (replyAddress != null) {
      writeString(buffer, replyAddress);
    } else {
      buffer.appendInt(0);
    }

    buffer.appendInt(sender.getPort());

    writeString(buffer, sender.getHost());
    encodeHeaders(buffer);
    encodeBody(buffer);
    buffer.setInt(0, buffer.length() - 4);
    return buffer;
  }

  @SuppressWarnings("unchecked")
  public void readFromWire(Buffer buffer, CodecManager codecManager) {
    // overall length already read when passed here
    int pos = 0;
    byte protocolVersion = buffer.getByte(pos);
    if (protocolVersion > WIRE_PROTOCOL_VERSION) {
      throw new IllegalStateException(
          "version must <= " + WIRE_PROTOCOL_VERSION);
    }
    pos++;
    byte systemCodecID = buffer.getByte(pos);
    pos++;
    if (systemCodecID != -1) {
      messageCodec = codecManager.getSystemCodes()[systemCodecID];
    } else {
      int length = buffer.getInt(pos);
      pos += 4;
      byte[] bytes = buffer.getBytes(pos, pos + length);
      String codecName = new String(bytes, CharsetUtil.UTF_8);
      messageCodec = codecManager.getCodec(codecName);
      if (messageCodec == null) {
        throw new IllegalStateException(
            "No message codec registered with name " + codecName);
      }
      pos += length;
    }
    byte sendByte = buffer.getByte(pos);
    pos++;
    send = sendByte == 0;

    int length = buffer.getInt(pos);
    pos += 4;
    byte[] bytes = buffer.getBytes(pos, pos + length);
    address = new String(bytes, CharsetUtil.UTF_8);
    pos += length;

    length = buffer.getInt(pos);
    pos += 4;
    if (length > 0) {
      bytes = buffer.getBytes(pos, pos + length);
      replyAddress = new String(bytes, CharsetUtil.UTF_8);
      pos += length;
    }
    int sendPort = buffer.getInt(pos);
    pos += 4;

    length = buffer.getInt(pos);
    pos += 4;
    bytes = buffer.getBytes(pos, pos + length);
    String sendHost = new String(bytes, CharsetUtil.UTF_8);
    pos += length;

    headerPos = pos;
    int headerLength = buffer.getInt(pos);
    pos += headerLength;
    bodyPos = pos;
    sender = new ServerID(sendPort, sendHost);
    wireBuffer = buffer;
    fromWire = true;
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

  public ServerID getSender() {
    return sender;
  }

  @Override
  public String replyAddress() {
    return replyAddress;
  }

  public boolean isFromWire() {
    return fromWire;
  }
}
