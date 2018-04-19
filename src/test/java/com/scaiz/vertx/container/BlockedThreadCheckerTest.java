package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class BlockedThreadCheckerTest {

  @Test
  public void test() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    long testInternal = 10;

    long maxExecTime = 50*1000000;

    BlockedThreadChecker checker = new BlockedThreadChecker(testInternal);
    VertxThreadFactory factory = new VertxThreadFactory("test", checker,
        false, maxExecTime);
    Thread thread = factory.newThread(() -> {
      Thread current = Thread.currentThread();
      if (current instanceof VertxThread) {
        VertxThread vt = (VertxThread) current;
        vt.executeStart();
        try {
          TimeUnit.MILLISECONDS.sleep(60);
        } catch (Exception ignored) {

        }
        vt.executeEnd();
        latch.countDown();
      }
    });
    thread.start();
    latch.await(1, TimeUnit.SECONDS);
  }
}
