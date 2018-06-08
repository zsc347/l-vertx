package com.scaiz.vertx.eventbus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.eventbus.cluster.FakeClusterManager;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ClusteredEventBusImplTest {

  private EventBus eventBus;

  @Before
  public void setUp() {
    VertxOptions options = new VertxOptions();
    options.setClustered(true);
    options.setClusterManager(new FakeClusterManager());
    CountDownLatch latch = new CountDownLatch(1);
    new VertxImpl(options, (ar) -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        eventBus = ar.result().eventBus();
        latch.countDown();
      }
    });
    try {
      latch.await();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testSendAndReceive() {
    final List<String> consumed = new LinkedList<>();
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.localConsumer("cluster.address", message -> {
      System.out.println(message);
      consumed.add((String) message.body());
      latch.countDown();
    });

    eventBus.send("cluster.address", "message1");

    try {
      latch.await(20, TimeUnit.MINUTES);
    } catch (Exception ignore) {
      fail();
    }

    assertTrue(consumed.size() == 1);
    assertTrue(consumed.contains("message1"));
  }
}
