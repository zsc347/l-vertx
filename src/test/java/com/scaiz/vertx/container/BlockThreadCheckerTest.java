package com.scaiz.vertx.container;

import com.scaiz.vertx.container.impl.BlockedThreadChecker;
import com.scaiz.vertx.container.impl.VertxThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class BlockThreadCheckerTest {

  @Test
  public void test() {
    long testInternal = 1000;
    long maxExecTime = 5000;
    BlockedThreadChecker checker = new BlockedThreadChecker(testInternal);
    VertxThreadFactory factory = new VertxThreadFactory("test", checker,
        false, maxExecTime);
    Thread thread = factory.newThread(() -> {
      Thread current = Thread.currentThread();
      if (current instanceof VertxThread) {
        VertxThread vthread = (VertxThread) current;
        vthread.executeStart();
        try {
          TimeUnit.SECONDS.sleep(6);
        } catch (Exception e) {

        }
        System.out.println("running in thread");
        vthread.executeEnd();
      }
    });
    thread.start();
  }

}
