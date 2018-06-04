package com.scaiz.vertx.eventbus.cluster;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.impl.clustered.ClusterManager;
import com.scaiz.vertx.eventbus.impl.clustered.NodeListener;
import com.scaiz.vertx.support.AsyncMultiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class FakeClusterManager implements ClusterManager {

  private static final Map<String, FakeClusterManager> nodes = Collections
      .synchronizedMap(new LinkedHashMap<>());

  private String nodeID;
  private VertxInternal vertx;
  private NodeListener nodeListener;


  private static void doJoin(String nodeID, FakeClusterManager node) {
    if (nodes.containsKey(nodeID)) {
      throw new IllegalStateException("Node has already joined");
    }
    nodes.put(nodeID, node);
    synchronized (nodes) {
      for (Entry<String, FakeClusterManager> entry : nodes.entrySet()) {
        if (!entry.getKey().equals(nodeID)) {
          new Thread(() -> entry.getValue().memberAdded(nodeID)).start();
        }
      }
    }
  }

  private static void doLeave(String nodeID) {
    nodes.remove(nodeID);
    synchronized (nodes) {
      for (Entry<String, FakeClusterManager> entry : nodes.entrySet()) {
        new Thread(() -> entry.getValue().memberLeft(nodeID)).start();
      }
    }
  }

  private synchronized void memberAdded(String nodeID) {
    if (isActive()) {
      try {
        if (nodeListener != null) {
          nodeListener.nodeAdded(nodeID);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private synchronized void memberLeft(String nodeID) {
    if (isActive()) {
      try {
        if (nodeListener != null) {
          nodeListener.nodeLeft(nodeID);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  @Override
  public <K, V> void getAsyncMultiMap(String name,
      Handler<AsyncResult<AsyncMultiMap<K, V>>> asyncResultHandler) {

  }

  @Override
  public String getNodeID() {
    return nodeID;
  }

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = (VertxInternal) vertx;
  }

  @Override
  public void join(Handler<AsyncResult<Void>> resultHandler) {
    vertx.getOrCreateContext().executeBlocking(fut -> {
      synchronized (this) {
        this.nodeID = UUID.randomUUID().toString();
        doJoin(nodeID, this);
      }
      fut.complete(null);
    }, true, resultHandler);
  }

  @Override
  public void leave(Handler<AsyncResult<Void>> resultHandler) {
    vertx.getOrCreateContext().executeBlocking(fut -> {
      synchronized (this) {
        doLeave(nodeID);
      }
    }, true, resultHandler);
  }


  @Override
  public void nodeListener(NodeListener listener) {
    this.nodeListener = nodeListener;
  }

  @Override
  public List<String> getNodes() {
    ArrayList<String> res;
    synchronized (nodes) {
      res = new ArrayList<>(nodes.keySet());
    }
    return res;
  }

  public boolean isActive() {
    return nodeID != null;
  }
}
