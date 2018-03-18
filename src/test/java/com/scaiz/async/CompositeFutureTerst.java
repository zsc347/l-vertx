package com.scaiz.async;

import com.scaiz.async.impl.CompositeFuture;
import com.scaiz.async.impl.FutureImpl;
import org.junit.Test;

public class CompositeFutureTerst {

  @Test
  public void testCompositeAll() {

    Future<String> future1 = new FutureImpl<>();
    future1.setHandler(ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
      }
    });

    Future<String> future2 = new FutureImpl<>();
    future2.setHandler(ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
      }
    });

    CompositeFuture.all(future1, future2).setHandler(ar -> System.out.println("sample3"));

    future1.complete("sample1");
    future2.complete("sample2");


  }
}
