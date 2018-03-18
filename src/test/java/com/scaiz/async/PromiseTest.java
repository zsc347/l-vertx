package com.scaiz.async;

import com.scaiz.async.impl.FutureImpl;
import org.junit.Test;

public class PromiseTest {

  @Test
  public void testPromiseSuccess() {
    Future<String> future = new FutureImpl<>();
    Promise<String> t = new Promise<>(future);

    t.then(ar -> {
      System.out.println(ar.result());
      return "sample2";
    }).then(ar -> {
      System.out.println(ar.result());
      return "sample3";
    }).then(ar -> {
      System.out.println(ar.result());
      return null;
    });

    t.then(ar -> {
      System.out.println(ar.result());
      return "again 1";
    }).then(ar -> {
      System.out.println(ar.result());
      return "again 2";
    }).then(ar -> {
      System.out.println(ar.result());
      return null;
    });

    future.complete("sample1");
  }
}
