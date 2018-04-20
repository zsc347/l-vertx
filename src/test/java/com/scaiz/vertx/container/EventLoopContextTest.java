package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class EventLoopContextTest {

  private EventLoopContext eventLoopContext;

  @Before
  public void setUp() {
    VertxImpl vertx = new VertxImpl();
    eventLoopContext = vertx.createEventLoopContext(null,
        Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void test() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    eventLoopContext.runOnContext((v) -> {
      for (int i = 0; i < 5; i++) {
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception ignored) {

        }
        System.out.println("handle " + i);
      }
      latch.countDown();
    });
    latch.await(1, TimeUnit.MINUTES);
  }
}
