package com.scaiz.vertx.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class EventBusImplTest {

  private EventBus eventBus;

  @Before
  public void setUp() {
    Vertx vertx = new VertxImpl();
    eventBus = vertx.eventBus();
  }

  @Test
  public void testSendAndReceive() {
    final List<String> consumed = new LinkedList<>();
    eventBus.localConsumer("test.address", message -> {
      consumed.add((String) message.body());
    });

    CountDownLatch latch = new CountDownLatch(1);
    eventBus.send("test.address", "message1");
    eventBus.send("test.address", "message2");
    eventBus.send("test.address", "message3", reply -> {
      if (reply.succeeded()) {
        latch.countDown();
      }
    });

    try {
      latch.await(20, TimeUnit.MILLISECONDS);
    } catch (Exception ignore) {
      fail();
    }

    assertTrue(consumed.size() == 3);
    assertTrue(consumed.contains("message1"));
    assertTrue(consumed.contains("message2"));
    assertTrue(consumed.contains("message3"));
  }


  @Test
  public void testSender() {
    CountDownLatch latch = new CountDownLatch(1);
    final List<String> consumed = new LinkedList<>();
    eventBus.localConsumer("test.address",
        message -> consumed.add((String) message.body()));
    MessageProducer<String> sender = eventBus.sender("test.address");
    sender.send("message", reply -> {
      if (reply.succeeded()) {
        latch.countDown();
      }
    });

    try {
      latch.await(2, TimeUnit.SECONDS);
    } catch (Exception ignore) {
      fail();
    }

    assertTrue(consumed.contains("message"));
  }


  // TODO test vertx
  // publisher send will consume credits, but will never get return
  // what if credits consumed out ?
  @Test
  public void testPublisher() {
    final List<String> consumed = new LinkedList<>();
    MessageProducer<String> publisher = eventBus.publisher("test.address");
    for (int i = 0; i < 3; i++) {
      eventBus.localConsumer("test.address",
          message -> consumed.add((String) message.body()));
    }
    publisher.send("send");
    assertEquals(1, consumed.stream().filter(x -> x.equals("send")).count());
    publisher.write("publish");
    assertEquals(3, consumed.stream().filter(x -> x.equals("publish")).count());
  }


  @Test
  public void testReply() {
    final List<String> replied = new LinkedList<>();
    CountDownLatch latch = new CountDownLatch(1);
    eventBus.localConsumer("test.address",
        message -> message.reply("reply-message"));
    eventBus.send("test.address", "should got reply message",
        new DeliveryOptions(),
        reply -> {
          if (reply.succeeded()) {
            replied.add((String) reply.result().body());
            latch.countDown();
          }
        });
    try {
      latch.await(20, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      fail();
    }
    assertTrue(replied.contains("reply-message"));
  }
}
