package com.scaiz.support;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class CaseInsensitiveHeaders implements MultiMap {

  private static final int BUCKET_SIZE = 17;
  private final MapEntry[] entries = new MapEntry[BUCKET_SIZE];
  private final MapEntry head = new MapEntry(-1, null, null);

  @Override
  public String get(CharSequence name) {
    return null;
  }

  @Override
  public String get(String name) {
    return null;
  }

  @Override
  public List<String> getAll(String name) {
    return null;
  }

  @Override
  public List<String> getAll(CharSequence name) {
    return null;
  }

  @Override
  public List<Entry<String, String>> entries() {
    return null;
  }

  @Override
  public boolean contains(String name) {
    return false;
  }

  @Override
  public boolean contains(CharSequence name) {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Set<String> names() {
  }

  @Override
  public MultiMap add(String name, String value) {
    return null;
  }

  @Override
  public MultiMap add(CharSequence name, CharSequence value) {
    return null;
  }

  @Override
  public MultiMap add(String name, Iterable<String> values) {
    return null;
  }

  @Override
  public MultiMap add(CharSequence name, Iterable<CharSequence> values) {
    return null;
  }

  @Override
  public MultiMap addAll(MultiMap map) {
    return null;
  }

  @Override
  public MultiMap addAll(Map<String, String> headers) {
    return null;
  }

  @Override
  public MultiMap set(String name, String value) {
    return null;
  }

  @Override
  public MultiMap set(CharSequence name, CharSequence value) {
    return null;
  }

  @Override
  public MultiMap set(String name, Iterable<String> values) {
    return null;
  }

  @Override
  public MultiMap set(CharSequence name, Iterable<CharSequence> values) {
    return null;
  }

  @Override
  public MultiMap setAll(MultiMap map) {
    return null;
  }

  @Override
  public MultiMap setAll(Map<String, String> headers) {
    return null;
  }

  @Override
  public MultiMap remove(String name) {
    return null;
  }

  @Override
  public MultiMap remove(CharSequence name) {
    return null;
  }

  @Override
  public MultiMap clear() {
    for (int i = 0; i < entries.length; i++) {
      entries[i] = null;
    }
    head.before = head.after = head;
    return this;
  }

  @Override
  public int size() {
    return names().size();
  }

  @Override
  public Iterator<Entry<String, String>> iterator() {
    return null;
  }

  private MultiMap set(Iterable<Map.Entry<String, String>> map) {
    clear();
    for (Map.Entry<String, String> entry : map) {
      add(entry.getKey(), entry.getValue());
    }
    return this;
  }

  private static final class MapEntry implements Map.Entry<String, String> {

    final int hash;
    final String key;
    String value;
    MapEntry before, after;

    MapEntry(int hash, String key, String value) {
      this.hash = hash;
      this.key = key;
      this.value = value;
    }

    void remove() {
      before.after = after;
      after.before = before;
    }

    void addBefore(MapEntry e) {
      after = e;
      before = e.before;
      before.after = this;
      after.before = this;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public String setValue(String value) {
      Objects.requireNonNull(value, "value");
      String oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    @Override
    public String toString() {
      return getKey() + ": " + getValue();
    }
  }
}
