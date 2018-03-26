package com.scaiz.json;

public class NumberHelper {

  static Integer toInteger(Number number) {
    if (number == null) {
      return null;
    } else if (number instanceof Integer) {
      return (Integer) number;
    } else {
      return number.intValue();
    }
  }

  static Long toLong(Number number) {
    if (number == null) {
      return null;
    } else if (number instanceof Long) {
      return (Long) number;
    } else {
      return number.longValue();
    }
  }

  static Float toFloat(Number number) {
    if (number == null) {
      return null;
    } else if (number instanceof Float) {
      return (Float) number;
    } else {
      return number.floatValue();
    }
  }

  static Double toDouble(Number number) {
    if (number == null) {
      return null;
    } else if (number instanceof Double) {
      return (Double) number;
    } else {
      return number.doubleValue();
    }
  }
}
