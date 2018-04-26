package com.scaiz.vertx.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.scaiz.vertx.container.impl.EventLoopContext;
import com.scaiz.vertx.impl.VertxImpl;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ContextTest {

  private EventLoopContext context;

  @Before
  public void setUp() {
    VertxImpl vertx = new VertxImpl();
    context = vertx.createEventLoopContext(null,
        Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void testRunOnContext() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    context.runOnContext((v) -> {
      for (int i = 0; i < 5; i++) {
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception ignored) {

        }
        System.out.println("handle " + i);
      }
      latch.countDown();
    });
    latch.await(1, TimeUnit.MINUTES);
  }

  @Test
  public void testExecuteUnorderedBlocking() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Throwable[] error = new Throwable[1];
    context.executeBlocking(f -> {
      try {
        assertTrue(ContextUtil.isOnWorkerThread());
        f.complete(1 + 2);
      } catch (Throwable e) {
        error[0] = e;
      } finally {
        latch.countDown();
      }
    }, false, r -> {
      try {
        assertEquals(r.result(), 3);
      } catch (Exception e) {
        error[0] = e;
      } finally {
        latch.countDown();
      }
    });
    latch.await(1, TimeUnit.MINUTES);
    if (error[0] != null) {
      throw new RuntimeException(error[0]);
    }
  }

  @Test
  public void testPutGetRemoveData() throws Exception {
    Object obj = new Object();
    Throwable[] error = new Throwable[1];
    CountDownLatch latch = new CountDownLatch(1);
    context.runOnContext(v -> {
      Context ctx = ContextUtil.getCurrentThreadContext();
      try {
        assertSame(ctx, context);
      } catch (Error e) {
        error[0] = e;
      }
      ctx.put("foo", obj);
      ctx.runOnContext(v2 -> {
        try {
          assertEquals(obj, ctx.get("foo"));
          assertTrue(ctx.remove("foo"));
        } catch (Error e) {
          error[0] = e;
        }
        ctx.runOnContext(v3 -> {
          try {
            assertNull(ctx.get("foo"));
          } catch (Exception e) {
            error[0] = e;
          }
          latch.countDown();
        });
      });
    });
    latch.await(1, TimeUnit.MINUTES);
    if (error[0] != null) {
      throw new RuntimeException(error[0]);
    }
  }


  @Test
  public void testContextExceptionHandler() throws Exception {
    RuntimeException failure = new RuntimeException();
    Throwable[] error = new Throwable[1];
    CountDownLatch latch = new CountDownLatch(1);

    context.exceptionHandler(err -> {
      try {
        assertSame(context, ContextUtil.getCurrentThreadContext());
        assertSame(failure, err);
      } catch (Error e) {
        error[0] = e;
      }
      latch.countDown();
    });

    context.runOnContext(v -> {
      throw failure;
    });

    latch.await(1, TimeUnit.MINUTES);
    if (error[0] != null) {
      throw new RuntimeException(error[0]);
    }
  }
}
