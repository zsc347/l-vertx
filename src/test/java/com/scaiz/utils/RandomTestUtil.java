package com.scaiz.utils;

import java.util.Random;

public class RandomTestUtil {

  public static Random random = new Random();

  public static long randomLong() {
    return random.nextLong();
  }
}
