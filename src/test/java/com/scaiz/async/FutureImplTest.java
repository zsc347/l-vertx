package com.scaiz.async;

import com.scaiz.async.impl.FutureImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FutureImplTest {

  @Test
  public void test() {
    Future<String> stringFuture = new FutureImpl<>();

    List<String> samples = Arrays.asList("Sample1", "Sample2");

    List<String> ordered = new ArrayList<>();

    stringFuture.setHandler(ar -> {
      if (ar.succeeded()) {
        ordered.add(ar.result());
      } else {

      }
    });

    stringFuture.complete(samples.get(0));

    stringFuture.setHandler(ar -> {
      if (ar.succeeded()) {
        ordered.add(samples.get(1));
      } else {
        throw new RuntimeException();
      }
    });

    assert ordered.get(0).equals(samples.get(0));
    assert ordered.get(1).equals(samples.get(1));
  }

}
