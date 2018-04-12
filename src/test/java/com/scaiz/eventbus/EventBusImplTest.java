package com.scaiz.eventbus;

import static org.junit.Assert.fail;

import com.scaiz.mock.VertxMock;
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
      } else {
        System.out.println("close succeed");
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
    registerConsumer();
    eventBus.send("test.address", "test message 1");
    eventBus.send("test.address", "test message 2");
    eventBus.send("test.address", "test message 3");
  }


  @Test
  public void testSender() {
    registerConsumer();
    MessageProducer<String> sender = eventBus.sender("test.address");
    sender.send("sender message should only consumed by one consumer - 1");
    sender.send("sender message should only consumed by one consumer - 2");
    sender.send("sender message should only consumed by one consumer - 3");
  }

  @Test
  public void testPublisher() {
    registerConsumer();
    MessageProducer<String> publisher = eventBus.publisher("test.address");

    // publisher send will consume credits, but will never get return
    // what if credits consumed out ?
    // TODO test vertx

    publisher.send("publisher send message"
        + " will be consumed by only one consumer");
    publisher.write("publisher write message should "
        + "be consumed by all consumer");
  }


  @Test
  public void testReply() {
    registerConsumer();
    boolean[] gotReply = new boolean[]{false};
    eventBus.send("test.address", "should got reply message",
        new DeliveryOptions(),
        reply -> {
          if (reply.succeeded()) {
            gotReply[0] = true;
            System.out.println("Message has been consumed successfully");
          }
        });
    if(!gotReply[0]) {
      fail();
    }
  }

  @After
  public void clearUp() {
    eventBus.close(null);
  }
}
