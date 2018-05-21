package com.scaiz.vertx.net;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.impl.VertxImpl;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class NetServerTest {

  @Test
  public void test() throws InterruptedException {
    Vertx vertx = new VertxImpl();
    NetServer server = vertx.createNetServer();
    CountDownLatch latch = new CountDownLatch(2);
    server.connectHandler(socket -> {
      socket.handler(buffer -> {
        System.out.println("Server receive " + buffer.length() + " : "
            + buffer.toString(StandardCharsets.UTF_8));
      });
    });
    server.listen(res -> {
      if (res.succeeded()) {
        System.out.println("Server is now listening ...");
      } else {
        System.out.println("Failed to bind ...");
      }
      latch.countDown();
    });
    latch.await(20, TimeUnit.MINUTES);
  }
}
