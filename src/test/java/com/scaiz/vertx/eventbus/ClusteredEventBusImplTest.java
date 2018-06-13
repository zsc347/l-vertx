package com.scaiz.vertx.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.VertxOptions;
import com.scaiz.vertx.eventbus.cluster.FakeClusterManager;
import com.scaiz.vertx.eventbus.impl.clustered.ClusterManager;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ClusteredEventBusImplTest {

  private EventBus eventBus;
  private EventBus eventBus2;

  @Before
  public void setUp() {
    ClusterManager fakeManager = new FakeClusterManager();
    VertxOptions options1 = new VertxOptions();
    options1.setClustered(true);
    options1.getEventBusOptions().setPort(8086);
    options1.setClusterManager(fakeManager);

    CountDownLatch latch = new CountDownLatch(2);
    new VertxImpl(options1, (ar) -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        eventBus = ar.result().eventBus();
        latch.countDown();
      }
    });

    VertxOptions options2 = new VertxOptions();
    options2.setClustered(true);
    options2.setClusterManager(fakeManager);
    options2.getEventBusOptions().setPort(8089);

    new VertxImpl(options2, (ar) -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        System.out.println(ar.cause().getMessage());
      } else {
        eventBus2 = ar.result().eventBus();
        latch.countDown();
      }
    });

    try {
      latch.await();
      System.out.println("init complete ...");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testSendAndReceive() {
    final List<String> consumed = new LinkedList<>();
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.consumer("cluster.address", message -> {
      consumed.add((String) message.body());
      System.out.println(message.body());

      message.reply("reply-1", ar -> {
        if (ar.succeeded()) {
          Message replyMessage = ar.result();
          consumed.add((String) replyMessage.body());
          System.out.println(replyMessage.body());
          latch.countDown();
        } else {
          ar.cause().printStackTrace();
          latch.countDown();
        }
      });
    }).completionHandler(ar -> {
      if (!ar.succeeded()) {
        ar.cause().printStackTrace();
        latch.countDown();
      }
    });

    eventBus2.send("cluster.address", "origin-message", reply -> {
      if (reply.succeeded()) {
        Message message = reply.result();
        consumed.add((String) message.body());
        message.reply("reply-2");
        System.out.println(message.body());
      } else {
        latch.countDown();
        reply.cause().printStackTrace();
      }
    });

    try {
      latch.await(20, TimeUnit.SECONDS);
    } catch (Exception ignore) {
      fail();
    }


    List<String> check = Arrays.asList("origin-message", "reply-1", "reply-2");
    assertEquals(check.size(), consumed.size());
    for(int i = 0; i < check.size(); i++) {
      assertEquals(consumed.get(i), check.get(i));
    }
  }
}
