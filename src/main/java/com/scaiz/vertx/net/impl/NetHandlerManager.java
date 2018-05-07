package com.scaiz.vertx.net.impl;

import com.scaiz.vertx.container.VertxEventLoopGroup;
import com.scaiz.vertx.container.impl.ContextImpl;
import io.netty.channel.EventLoop;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetHandlerManager<T> {

  private final VertxEventLoopGroup availableWorkers;
  private final ConcurrentMap<EventLoop, NetHandlerHolders> handlerMap
      = new ConcurrentHashMap<>();

  private volatile boolean hasHandlers;

  public NetHandlerManager(VertxEventLoopGroup availableWorkers) {
    this.availableWorkers = availableWorkers;
  }

  public boolean hasHandlers() {
    return hasHandlers;
  }

  public NetHandlerHolder<T> chooseHandler(EventLoop worker) {
    NetHandlerHolders<T> handlers = handlerMap.get(worker);
    return handlers == null ? null : handlers.chooseHandler();
  }

  public synchronized void addHandler(T handler, ContextImpl context) {
    EventLoop worker = context.nettyEventLoop();
    availableWorkers.addWorker(worker);
    NetHandlerHolders<T> handlers = new NetHandlerHolders<>();
    NetHandlerHolders<T> prev = handlerMap.putIfAbsent(worker, handlers);
    if (prev != null) {
      handlers = prev;
    }
    handlers.addHandler(new NetHandlerHolder<>(context, handler));
    hasHandlers = true;
  }

  public synchronized void removeHandler(T handler, ContextImpl context) {
    EventLoop worker = context.nettyEventLoop();
    NetHandlerHolders<T> handlers = handlerMap.get(worker);
    if (!handlers.removeHandler(new NetHandlerHolder<>(context, handler))) {
      throw new IllegalStateException("handler not found");
    }
    if (handlers.isEmpty()) {
      handlerMap.remove(worker);
    }
    if (handlerMap.isEmpty()) {
      hasHandlers = false;
    }
    availableWorkers.removeWorker(worker);
  }

}
