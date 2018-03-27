package com.scaiz.utils;

import com.scaiz.buffer.Buffer;

public class BufferTestUtil {

  public static boolean byteArraysEqual(byte[] b1, byte[] b2) {
    if (b1.length != b2.length) return false;
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) return false;
    }
    return true;
  }

  public static byte randomByte() {
    return (byte) ((int) (Math.random() * 255) - 128);
  }

  public static byte[] randomByteArray(int length) {
    byte[] line = new byte[length];
    for (int i = 0; i < length; i++) {
      byte rand;
      rand = randomByte();
      line[i] = rand;
    }
    return line;
  }

  public static Buffer randomBuffer(int length) {
    return Buffer.buffer(randomByteArray(length));
  }

  public static String randomAlphaString(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char c = (char) (65 + 25 * Math.random());
      builder.append(c);
    }
    return builder.toString();
  }

  public static String randomUnicodeString(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char c;
      do {
        c = (char) (0xFFFF * Math.random());
      } while ((c >= 0xFFFE) || (c >= 0xD800 && c <= 0xDFFF)); // Illegal chars
      builder.append(c);
    }
    return builder.toString();
  }
}