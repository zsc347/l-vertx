CountDownLatch和CyclicBarrier都提供了多个线程之间等待协同的能力。

区别是，CountDownLatch用于
一个线程等待其他多个线程执行到某个状态，而CyclicBarrier可以使多个线程互相等待直到到达某个状态后
再继续往下执行。

CountDownLatch基于AbstractQueuedSynchronizer实现。

AbstractQueuedSynchronizer简介：
Provides a framework for implementing blocking locks and related
synchronizers (semaphores, events, etc) that rely on
first-in-first-out (FIFO) wait queues.  This class is designed to
be a useful basis for most kinds of synchronizers that rely on a
single atomic {@code int} value to represent state.


CountDownLatch的实现

````java
public class CountDownLatch {
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;


    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }


    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }


    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        sync.releaseShared(1);
    }
}

````
