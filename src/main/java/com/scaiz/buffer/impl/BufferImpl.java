package com.scaiz.buffer.impl;

import com.scaiz.buffer.Buffer;
import com.scaiz.json.JsonArray;
import com.scaiz.json.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BufferImpl implements Buffer {

  private ByteBuf byteBuf;

  public BufferImpl() {
    this(0);
  }

  BufferImpl(int initialSizeHint) {
    byteBuf = Unpooled.unreleasableBuffer(
        Unpooled.buffer(initialSizeHint, Integer.MAX_VALUE));
  }

  BufferImpl(byte[] bytes) {
    byteBuf = Unpooled.unreleasableBuffer(
        Unpooled.buffer(bytes.length, Integer.MAX_VALUE)).writeBytes(bytes);
  }

  BufferImpl(String str, String enc) {
    this(str.getBytes(Charset.forName(Objects.requireNonNull(enc))));
  }

  BufferImpl(String str, Charset cs) {
    this(str.getBytes(cs));
  }

  BufferImpl(String str) {
    this(str, StandardCharsets.UTF_8);
  }

  BufferImpl(ByteBuf buffer) {
    this.byteBuf = Unpooled.unreleasableBuffer(buffer);
  }

  @Override
  public String toString() {
    return byteBuf.toString(StandardCharsets.UTF_8);
  }

  @Override
  public String toString(String enc) {
    return byteBuf.toString(Objects.requireNonNull(Charset.forName(enc)));
  }

  @Override
  public String toString(Charset enc) {
    return byteBuf.toString(Objects.requireNonNull(enc));
  }

  @Override
  public JsonObject toJsonObject() {
    return new JsonObject(this);
  }

  @Override
  public JsonArray toJsonArray() {
    return new JsonArray(this);
  }

  @Override
  public byte getByte(int pos) {
    return byteBuf.getByte(pos);
  }

  @Override
  public short getUnsignedByte(int pos) {
    return byteBuf.getUnsignedByte(pos);
  }

  @Override
  public int getInt(int pos) {
    return byteBuf.getInt(pos);
  }

  @Override
  public int getIntLE(int pos) {
    return byteBuf.getIntLE(pos);
  }

  @Override
  public long getUnsignedInt(int pos) {
    return byteBuf.getUnsignedInt(pos);
  }

  @Override
  public long getUnsignedIntLE(int pos) {
    return byteBuf.getUnsignedIntLE(pos);
  }

  @Override
  public long getLong(int pos) {
    return byteBuf.getLong(pos);
  }

  @Override
  public long getLongLE(int pos) {
    return byteBuf.getLongLE(pos);
  }

  @Override
  public double getDouble(int pos) {
    return byteBuf.getDouble(pos);
  }

  @Override
  public float getFloat(int pos) {
    return byteBuf.getFloat(pos);
  }

  @Override
  public short getShort(int pos) {
    return byteBuf.getShort(pos);
  }

  @Override
  public short getShortLE(int pos) {
    return byteBuf.getShortLE(pos);
  }

  @Override
  public int getUnsignedShort(int pos) {
    return byteBuf.getUnsignedShortLE(pos);
  }

  @Override
  public int getUnsignedShortLE(int pos) {
    return byteBuf.getUnsignedShortLE(pos);
  }

  @Override
  public int getMedium(int pos) {
    return byteBuf.getMedium(pos);
  }

  @Override
  public int getMediumLE(int pos) {
    return byteBuf.getMediumLE(pos);
  }

  @Override
  public int getUnsignedMedium(int pos) {
    return byteBuf.getUnsignedMedium(pos);
  }

  @Override
  public int getUnsignedMediumLE(int pos) {
    return byteBuf.getUnsignedMediumLE(pos);
  }

  @Override
  public byte[] getBytes() {
    byte[] arr = new byte[byteBuf.writerIndex()];
    byteBuf.getBytes(0, arr);
    return arr;
  }

  @Override
  public byte[] getBytes(int start, int end) {
    assertEndGteStart(end < start);
    byte[] arr = new byte[end - start];
    byteBuf.getBytes(start, arr);
    return arr;
  }

  @Override
  public Buffer getBytes(byte[] dst) {
    byteBuf.getBytes(0, dst, 0, byteBuf.writerIndex());
    return this;
  }

  @Override
  public Buffer getBytes(byte[] dst, int dstIndex) {
    byteBuf.getBytes(0, dst, dstIndex, byteBuf.writerIndex());
    return this;
  }

  @Override
  public Buffer getBytes(int start, int end, byte[] dst) {
    assertEndGteStart(end < start);
    byteBuf.getBytes(start, dst, 0, end - start);
    return this;
  }

  @Override
  public Buffer getBytes(int start, int end, byte[] dst, int dstIndex) {
    assertEndGteStart(end < start);
    byteBuf.getBytes(start, dst, dstIndex, end - start);
    return this;
  }

  private void assertEndGteStart(boolean check) {
    if (!check) {
      throw new IllegalArgumentException(
          "end must be greater or equal than start");
    }
  }

  @Override
  public Buffer getBuffer(int start, int end) {
    return new BufferImpl(getBytes(start, end));
  }

  @Override
  public String getString(int start, int end, String enc) {
    byte[] bytes = getBytes(start, end);
    Charset cs = Charset.forName(enc);
    return new String(bytes, cs);
  }

  @Override
  public String getString(int start, int end) {
    byte[] bytes = getBytes(start, end);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public Buffer appendBuffer(Buffer buff) {
    byteBuf.writeBytes(buff.getByteBuf());
    return this;
  }

  @Override
  public Buffer appendBuffer(Buffer buff, int offset, int len) {
    ByteBuf fromBuf = buff.getByteBuf();
    byteBuf.writeBytes(fromBuf, fromBuf.readerIndex() + offset, len);
    return this;
  }

  @Override
  public Buffer appendBytes(byte[] bytes) {
    byteBuf.writeBytes(bytes);
    return this;
  }

  @Override
  public Buffer appendBytes(byte[] bytes, int offset, int len) {
    byteBuf.writeBytes(bytes, offset, len);
    return this;
  }

  @Override
  public Buffer appendByte(byte b) {
    byteBuf.writeByte(b);
    return this;
  }

  @Override
  public Buffer appendUnsignedByte(short b) {
    byteBuf.writeByte(b);
    return this;
  }

  @Override
  public Buffer appendInt(int i) {
    byteBuf.writeInt(i);
    return this;
  }

  @Override
  public Buffer appendIntLE(int i) {
    byteBuf.writeIntLE(i);
    return this;
  }

  @Override
  public Buffer appendUnsignedInt(long i) {
    byteBuf.writeInt((int) i);
    return this;
  }

  @Override
  public Buffer appendUnsignedIntLE(long i) {
    byteBuf.writeIntLE((int) i);
    return this;
  }

  @Override
  public Buffer appendMedium(int i) {
    byteBuf.writeMedium(i);
    return this;
  }

  @Override
  public Buffer appendMediumLE(int i) {
    byteBuf.writeMediumLE(i);
    return this;
  }

  @Override
  public Buffer appendLong(long l) {
    byteBuf.writeLong(l);
    return this;
  }

  public Buffer appendLongLE(long l) {
    byteBuf.writeLongLE(l);
    return this;
  }

  @Override
  public Buffer appendShort(short s) {
    byteBuf.writeShort(s);
    return this;
  }

  @Override
  public Buffer appendShortLE(short s) {
    byteBuf.writeShortLE(s);
    return this;
  }

  @Override
  public Buffer appendUnsignedShort(int s) {
    byteBuf.writeShort(s);
    return this;
  }

  @Override
  public Buffer appendUnsignedShortLE(int s) {
    byteBuf.writeShortLE(s);
    return this;
  }

  @Override
  public Buffer appendFloat(float f) {
    byteBuf.writeFloat(f);
    return this;
  }

  @Override
  public Buffer appendDouble(double d) {
    byteBuf.writeDouble(d);
    return this;
  }

  private Buffer append(String str, Charset charset) {
    byte[] bytes = str.getBytes(charset);
    byteBuf.writeBytes(bytes);
    return this;
  }

  @Override
  public Buffer appendString(String str, String enc) {
    return append(str, Charset.forName(Objects.requireNonNull(enc)));
  }

  @Override
  public Buffer appendString(String str) {
    return append(str, StandardCharsets.UTF_8);
  }

  private void ensureWritable(int pos, int len) {
    int ni = pos + len;
    int cap = byteBuf.capacity();
    int over = ni - cap;
    if (over > 0) {
      byteBuf.writerIndex(cap);
      byteBuf.ensureWritable(over);
    }
    // We have to make sure that the writerIndex is always positioned on the last bit of data set in the buffer
    if (ni > byteBuf.writerIndex()) {
      byteBuf.writerIndex(ni);
    }
  }

  @Override
  public Buffer setByte(int pos, byte b) {
    ensureWritable(pos, 1);
    byteBuf.setByte(pos, b);
    return this;
  }

  @Override
  public Buffer setUnsignedByte(int pos, short b) {
    ensureWritable(pos, 1);
    byteBuf.setByte(pos, b);
    return this;
  }

  @Override
  public Buffer setInt(int pos, int i) {
    ensureWritable(pos, 4);
    byteBuf.setInt(pos, i);
    return this;
  }

  @Override
  public Buffer setIntLE(int pos, int i) {
    ensureWritable(pos, 4);
    byteBuf.setIntLE(pos, i);
    return this;
  }

  @Override
  public Buffer setUnsignedInt(int pos, long i) {
    ensureWritable(pos, 4);
    byteBuf.setInt(pos, (int) i);
    return this;
  }

  @Override
  public Buffer setUnsignedIntLE(int pos, long i) {
    ensureWritable(pos, 4);
    byteBuf.setIntLE(pos, (int) i);
    return this;
  }

  @Override
  public Buffer setMedium(int pos, int i) {
    ensureWritable(pos, 3);
    byteBuf.setMedium(pos, i);
    return this;
  }

  @Override
  public Buffer setMediumLE(int pos, int i) {
    ensureWritable(pos, 3);
    byteBuf.setMediumLE(pos, i);
    return this;
  }

  public Buffer setLong(int pos, long l) {
    ensureWritable(pos, 8);
    byteBuf.setLong(pos, l);
    return this;
  }

  public Buffer setLongLE(int pos, long l) {
    ensureWritable(pos, 8);
    byteBuf.setLongLE(pos, l);
    return this;
  }

  @Override
  public Buffer setDouble(int pos, double d) {
    ensureWritable(pos, 8);
    byteBuf.setDouble(pos, d);
    return this;
  }

  @Override
  public Buffer setFloat(int pos, float f) {
    ensureWritable(pos, 4);
    byteBuf.setFloat(pos, f);
    return this;
  }

  @Override
  public Buffer setShort(int pos, short s) {
    ensureWritable(pos, 2);
    byteBuf.setShort(pos, s);
    return this;
  }

  @Override
  public Buffer setShortLE(int pos, short s) {
    ensureWritable(pos, 2);
    byteBuf.setShortLE(pos, s);
    return this;
  }

  public Buffer setUnsignedShort(int pos, int s) {
    ensureWritable(pos, 2);
    byteBuf.setShort(pos, s);
    return this;
  }

  public Buffer setUnsignedShortLE(int pos, int s) {
    ensureWritable(pos, 2);
    byteBuf.setShortLE(pos, s);
    return this;
  }

  @Override
  public Buffer setBuffer(int pos, Buffer b) {
    ensureWritable(pos, b.length());
    byteBuf.setBytes(pos, b.getByteBuf());
    return this;
  }

  @Override
  public Buffer setBuffer(int pos, Buffer b, int offset, int len) {
    ensureWritable(pos, len);
    ByteBuf byteBuf = b.getByteBuf();
    byteBuf.setBytes(pos, byteBuf, byteBuf.readerIndex() + offset, len);
    return this;
  }

  @Override
  public Buffer setBytes(int pos, ByteBuffer b) {
    ensureWritable(pos, b.limit());
    byteBuf.setBytes(pos, b);
    return this;
  }

  @Override
  public Buffer setBytes(int pos, byte[] b) {
    ensureWritable(pos, b.length);
    byteBuf.setBytes(pos, b);
    return this;
  }

  @Override
  public Buffer setBytes(int pos, byte[] b, int offset, int len) {
    ensureWritable(pos, len);
    byteBuf.setBytes(pos, b, offset, len);
    return this;
  }

  private Buffer setBytes(int pos, String str, Charset charset) {
    byte[] bytes = str.getBytes(charset);
    ensureWritable(pos, bytes.length);
    byteBuf.setBytes(pos, bytes);
    return this;
  }

  @Override
  public Buffer setString(int pos, String str) {
    return setBytes(pos, str, StandardCharsets.UTF_8);
  }

  @Override
  public Buffer setString(int pos, String str, String enc) {
    return setBytes(pos, str, Charset.forName(enc));
  }

  @Override
  public int length() {
    return byteBuf.writerIndex();
  }

  @Override
  public Buffer copy() {
    return new BufferImpl(byteBuf.copy());
  }

  @Override
  public Buffer slice() {
    return new BufferImpl(byteBuf.slice());
  }

  @Override
  public Buffer slice(int start, int end) {
    return new BufferImpl(byteBuf.slice(start, end - start));
  }

  @Override
  public ByteBuf getByteBuf() {
    return byteBuf.duplicate();
  }
}
