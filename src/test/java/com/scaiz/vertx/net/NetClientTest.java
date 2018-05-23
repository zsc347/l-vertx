package com.scaiz.vertx.net;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.impl.VertxImpl;
import io.netty.util.CharsetUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class NetClientTest {

  @Test
  public void testEchoClient() throws InterruptedException {
    Vertx vertx = new VertxImpl();
    NetClient client = vertx.createNetClient();
    CountDownLatch latch = new CountDownLatch(2);

    client.connect(8086, "127.0.0.1", ar -> {
      if (ar.succeeded()) {
        NetSocket sock = ar.result();
        sock.handler(buffer ->
            System.out.println(
                "Server response: " + buffer.toString(CharsetUtil.UTF_8)));
        new Thread(() -> {
          for (int i = 0; ; i++) {
            try {
              String message = "message " + i;
              System.out.println("Client send: " + message);
              sock.write(message);
              TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }).start();
      } else {
        client.close();
      }
    });
    latch.await(20, TimeUnit.MINUTES);
  }
}
