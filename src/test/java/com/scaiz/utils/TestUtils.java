package com.scaiz.utils;

public class TestUtils {

  public static void assertNullPointerException(Runnable runnable) {
    try {
      runnable.run();
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // OK
    }
  }

  public static void assertIllegalArgumentException(Runnable runnable) {
    try {
      runnable.run();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // OK
    }
  }

  public static void assertIndexOutOfBoundsException(Runnable runnable) {
    try {
      runnable.run();
      fail("Should throw IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // OK
    }
  }

  static public void fail(String message) {
    if (message == null) {
      throw new AssertionError();
    }
    throw new AssertionError(message);
  }
}
