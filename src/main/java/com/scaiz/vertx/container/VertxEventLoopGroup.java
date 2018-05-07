package com.scaiz.vertx.container;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class VertxEventLoopGroup extends
    AbstractEventExecutorGroup implements
    EventLoopGroup {

  private int pos;
  private final List<EventLoopHolder> workers = new ArrayList<>();


  public synchronized void addWorker(EventLoop worker) {
    EventLoopHolder holder = findHolder(worker);
    if (holder == null) {
      workers.add(new EventLoopHolder(worker));
    } else {
      holder.count++;
    }
  }

  public synchronized void removeWorker(EventLoop worker) {
    EventLoopHolder holder = findHolder(worker);
    if (holder != null) {
      holder.count--;
      if (holder.count == 0) {
        workers.remove(holder);
      }
      checkPos();
    } else {
      throw new IllegalStateException("worker not exist");
    }
  }

  public synchronized int workerCount() {
    return workers.size();
  }

  private EventLoopHolder findHolder(EventLoop worker) {
    for (EventLoopHolder holder : workers) {
      if (holder.worker.equals(worker)) {
        return holder;
      }
    }
    return null;
  }

  private void checkPos() {
    if (pos == workers.size()) {
      pos = 0;
    }
  }

  @Override
  public EventLoop next() {
    if (workers.isEmpty()) {
      throw new IllegalStateException();
    } else {
      EventLoop worker = workers.get(pos).worker;
      pos++;
      checkPos();
      return worker;
    }
  }

  @Override
  public ChannelFuture register(Channel channel) {
    return next().register(channel);
  }

  @Override
  public ChannelFuture register(ChannelPromise promise) {
    return next().register(promise);
  }

  @Override
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    throw new UnsupportedOperationException(
        "use #register(ChannelPromise) instead");
  }


  @Override
  public boolean isShuttingDown() {
    return false;
  }

  @Override
  public Future<?> shutdownGracefully(long quietPeriod, long timeout,
      TimeUnit unit) {
    throw new UnsupportedOperationException("should never be called");
  }

  @Override
  public Future<?> terminationFuture() {
    throw new UnsupportedOperationException("should never be called");
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException("should never be called");

  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return isShutdown();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException("should never be called");
  }

  @Override
  public Iterator<EventExecutor> iterator() {
    return new EventLoopIterator(workers.iterator());
  }

  private static class EventLoopHolder {

    int count = 1;
    final EventLoop worker;

    EventLoopHolder(EventLoop worker) {
      this.worker = worker;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      EventLoopHolder that = (EventLoopHolder) o;
      return this.worker != null
          ? this.worker.equals(that.worker)
          : that.worker == null;
    }

    @Override
    public int hashCode() {
      return worker != null ? worker.hashCode() : 0;
    }
  }

  private static final class EventLoopIterator implements
      Iterator<EventExecutor> {

    private final Iterator<EventLoopHolder> holderIter;

    EventLoopIterator(Iterator<EventLoopHolder> holderIter) {
      this.holderIter = holderIter;
    }

    @Override
    public boolean hasNext() {
      return holderIter.hasNext();
    }

    @Override
    public EventExecutor next() {
      return holderIter.next().worker;
    }
  }
}
