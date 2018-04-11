package com.scaiz.eventbus;

import static org.junit.Assert.*;

import com.scaiz.mock.VertxMock;
import org.junit.Assert;
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

  @Test
  public void testSendAndReceive() {
    MessageConsumer<String> consumer = eventBus.consumer("test.address");
    consumer.handler(message ->
        System.out.println("I have received a message: " + message.body()));
    eventBus.send("test.address", "test-message");
  }

}
