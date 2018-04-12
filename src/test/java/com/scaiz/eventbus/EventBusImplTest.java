package com.scaiz.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.scaiz.mock.VertxMock;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventBusImplTest {

  private EventBus eventBus;

  @Before
  public void setUp() {
    eventBus = new VertxMock().eventBus();
    eventBus.start(ar -> {
      if (ar.failed()) {
        fail();
      }
    });
  }

  @Test
  public void testClose() {
    eventBus.close(ar -> {
      if (ar.failed()) {
        fail();
      }
    });
  }


  private void registerConsumer() {
    MessageConsumer<String> consumer = eventBus.consumer("test.address");
    consumer.handler(message ->
        System.out.println("Receiver 1: " + message.body()));

    eventBus.consumer("test.address", message -> {
      System.out.println("Receiver 2: " + message.body());
    });
  }


  @Test
  public void testSendAndReceive() {
    final List<String> consumed = new LinkedList<>();
    eventBus.consumer("test.address", message -> {
      consumed.add((String) message.body());
    });
    eventBus.send("test.address", "message1");
    eventBus.send("test.address", "message2");
    eventBus.send("test.address", "message3");
    assertTrue(consumed.size() == 3);
    assertTrue(consumed.contains("message1"));
    assertTrue(consumed.contains("message2"));
    assertTrue(consumed.contains("message3"));
  }


  @Test
  public void testSender() {
    final List<String> consumed = new LinkedList<>();
    eventBus.consumer("test.address",
        message -> consumed.add((String) message.body()));
    MessageProducer<String> sender = eventBus.sender("test.address");
    sender.send("message");
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
      eventBus.consumer("test.address",
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
    eventBus.consumer("test.address",
        message -> message.reply("reply-message"));
    eventBus.send("test.address", "should got reply message",
        new DeliveryOptions(),
        reply -> {
          if (reply.succeeded()) {
            replied.add((String) reply.result().body());
          }
        });
    assertTrue(replied.contains("reply-message"));
  }

  @After
  public void clearUp() {
    eventBus.close(null);
  }
}
