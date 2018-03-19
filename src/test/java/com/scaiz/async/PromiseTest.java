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
      return "schedule-1";
    }).then(ar -> {
      System.out.println(ar.result());
      return "schedule-2";
    }).then(ar -> {
      System.out.println(ar.result());
      return null;
    });

    future.complete("start");

    t.then(ar -> {
      System.out.println(ar.result());
      return "again-1";
    }).then(ar -> {
      System.out.println(ar.result());
      return "again-2";
    }).then(ar -> {
      System.out.println(ar.result());
      return null;
    });


  }

  @Test
  public void testResolve() {
    Promise<String> res = Promise.resolve("resolve");

    res.then(ar -> {
      System.out.println("then");
      return null;
    }).thenCatch(ar -> {
      System.out.println("thenCatch");
      return null;
    });

    res.thenCatch(ar -> {
      System.out.println("thenCatch");
      return null;
    });
  }

  @Test
  public void testReject() {
    Promise<String> res = Promise.reject(new RuntimeException("reject"));
    res.then(ar -> {
      System.out.println("then");
      return null;
    });

    res.thenCatch(ar -> {
      System.out.println("thenCatch");
      return "after cached resolved promise";
    }).then(ar -> {
      System.out.println(ar.result());
      return null;
    });
  }
}
