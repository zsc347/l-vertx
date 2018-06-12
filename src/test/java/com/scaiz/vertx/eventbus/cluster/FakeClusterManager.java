package com.scaiz.vertx.eventbus.cluster;

import com.scaiz.vertx.Vertx;
import com.scaiz.vertx.async.AsyncResult;
import com.scaiz.vertx.async.Future;
import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.container.impl.ContextImpl;
import com.scaiz.vertx.container.impl.TaskQueue;
import com.scaiz.vertx.eventbus.impl.clustered.ClusterManager;
import com.scaiz.vertx.eventbus.impl.clustered.NodeListener;
import com.scaiz.vertx.support.AsyncMultiMap;
import com.scaiz.vertx.support.ChoosableIterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

public class FakeClusterManager implements ClusterManager {

  private static final Map<String, FakeClusterManager> nodes = Collections
      .synchronizedMap(new LinkedHashMap<>());
  private static ConcurrentMap<String, ConcurrentMap> asyncMultiMaps
      = new ConcurrentHashMap<>();

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
      Handler<AsyncResult<AsyncMultiMap<K, V>>> resultHandler) {
    ConcurrentMap map = asyncMultiMaps.get(name);
    if (map == null) {
      map = new ConcurrentHashMap<>();
      ConcurrentMap prevMap = asyncMultiMaps.putIfAbsent(name, map);
      if (prevMap != null) {
        map = prevMap;
      }
    }
    @SuppressWarnings("unchecked")
    ConcurrentMap<K, ChoosableSet<V>> theMap = map;
    vertx.runOnContext(v -> resultHandler
        .handle(Future.succeededFuture(new FakeAsyncMultiMap<>(theMap))));
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
  public void nodeListener(NodeListener nodeListener) {
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


  private class FakeAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private final ConcurrentMap<K, ChoosableSet<V>> map;
    private final TaskQueue taskQueue;

    FakeAsyncMultiMap(
        ConcurrentMap<K, ChoosableSet<V>> map) {
      this.map = map;
      taskQueue = new TaskQueue();
    }

    @Override
    public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
      ContextImpl ctx = vertx.getOrCreateContext();
      ctx.executeBlocking(fut -> {
        ChoosableSet<V> vals = map.get(k);
        if (vals == null) {
          vals = new ChoosableSet<>(1);
          ChoosableSet<V> prevVals = map.putIfAbsent(k, vals);
          if (prevVals != null) {
            vals = prevVals;
          }
        }
        vals.add(v);
        fut.complete(null);
      }, taskQueue, completionHandler);
    }

    @Override
    public void get(K k,
        Handler<AsyncResult<ChoosableIterable<V>>> resultHandler) {
      vertx.getOrCreateContext().executeBlocking(fut -> {
        ChoosableIterable<V> it = map.get(k);
        if (it == null) {
          it = new ChoosableSet<>(0);
        }
        fut.complete(it);
      }, taskQueue, resultHandler);
    }

    @Override
    public void remove(K k, V v,
        Handler<AsyncResult<Boolean>> completionHandler) {
      vertx.getOrCreateContext().executeBlocking(fut -> {
        ChoosableSet<V> vals = map.get(k);
        boolean found = false;
        if (vals != null) {
          boolean removed = vals.remove(v);
          if (removed) {
            if (vals.isEmpty()) {
              map.remove(k);
            }
            found = true;
          }
        }
        fut.complete(found);
      }, taskQueue, completionHandler);
    }

    @Override
    public void removeAllForValue(V v,
        Handler<AsyncResult<Void>> completionHandler) {
      removeAllMatching(v::equals, completionHandler);
    }

    @Override
    public void removeAllMatching(Predicate<V> p,
        Handler<AsyncResult<Void>> completionHandler) {
      vertx.getOrCreateContext().executeBlocking(fut -> {
        Iterator<Entry<K, ChoosableSet<V>>> mapIter =
            map.entrySet().iterator();
        while (mapIter.hasNext()) {
          Entry<K, ChoosableSet<V>> entry = mapIter.next();
          ChoosableSet<V> vals = entry.getValue();
          Iterator<V> iter = vals.iterator();
          while (iter.hasNext()) {
            V val = iter.next();
            if (p.test(val)) {
              iter.remove();
            }
            if (vals.isEmpty()) {
              mapIter.remove();
            }
          }
        }
        fut.complete(null);
      }, taskQueue, completionHandler);
    }
  }
}
