package com.scaiz.vertx.eventbus.cluster;

import com.scaiz.vertx.support.ChoosableIterable;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChoosableSet<T> implements ChoosableIterable<T> {

  private final Set<T> ids;
  private volatile Iterator<T> iter;

  public ChoosableSet(int initialSize) {
    ids = Collections.newSetFromMap(new ConcurrentHashMap<>(initialSize));
  }

  public int size() {
    return ids.size();
  }

  public void add(T item) {
    ids.add(item);
  }

  public boolean remove(T item) {
    return ids.remove(item);
  }

  @Override
  public boolean isEmpty() {
    return ids.isEmpty();
  }

  @Override
  public T choose() {
    if (ids.isEmpty()) {
      return null;
    }
    if (iter == null || !iter.hasNext()) {
      iter = ids.iterator();
    }
    try {
      return iter.next();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Iterator<T> iterator() {
    return ids.iterator();
  }
}
