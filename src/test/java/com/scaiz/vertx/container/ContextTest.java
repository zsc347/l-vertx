package com.scaiz.vertx.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ContextTest {

  private EventLoopContext context;

  @Before
  public void setUp() {
    VertxImpl vertx = new VertxImpl();
    context = vertx.createEventLoopContext(null,
        Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void testRunOnContext() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    context.runOnContext((v) -> {
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

  @Test
  public void testExecuteUnorderedBlocking() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Throwable[] error = new Throwable[1];
    context.executeBlocking(f -> {
      try {
        assertTrue(ContextUtil.isOnWorkerThread());
        f.complete(1 + 2);
      } catch (Throwable e) {
        error[0] = e;
      } finally {
        latch.countDown();
      }
    }, false, r -> {
      try {
        assertEquals(r.result(), 3);
      } catch (Exception e) {
        error[0] = e;
      } finally {
        latch.countDown();
      }
    });
    latch.await(1, TimeUnit.MINUTES);
    if (error[0] != null) {
      throw new RuntimeException(error[0]);
    }
  }
}
