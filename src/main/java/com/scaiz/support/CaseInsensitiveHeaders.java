package com.scaiz.support;

import java.util.Iterator;
import java.util.LinkedList;
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

  public CaseInsensitiveHeaders() {
    head.after = head.before = head;
  }

  @Override
  public String get(CharSequence name) {
    return get(name.toString());
  }

  @Override
  public List<String> getAll(CharSequence name) {
    return getAll(name.toString());
  }

  @Override
  public String get(String name) {
    Objects.requireNonNull(name);
    int h = hash(name);
    int i = index(h);
    MapEntry e = entries[i];
    while (e != null) {
      if (e.hash == h && eq(name, e.key)) {
        return e.getValue();
      }
      e = e.next;
    }
    return null;
  }

  @Override
  public List<String> getAll(String name) {
    LinkedList<String> rs = new LinkedList<>();
    int h = hash(name);
    int i = index(h);
    MapEntry e = entries[i];
    while (e != null) {
      if (e.hash == h && eq(name, e.key)) {
        rs.addFirst(e.getValue());
      }
      e = e.next;
    }
    return rs;
  }


  @Override
  public List<Entry<String, String>> entries() {
    List<Map.Entry<String, String>> all = new LinkedList<>();
    MapEntry e = head.after;
    while (e != head) {
      all.add(e);
      e = e.after;
    }
    return all;
  }

  @Override
  public boolean contains(String name) {
    return get(name) != null;
  }

  @Override
  public boolean contains(CharSequence name) {
    return get(name) != null;
  }

  @Override
  public boolean isEmpty() {
    return head.after == head;
  }

  @Override
  public Set<String> names() {
    Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    MapEntry e = head.after;
    while (e != head) {
      names.add(e.getKey());
      e = e.after;
    }
    return names;
  }

  @Override
  public MultiMap add(String name, String strVal) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(strVal);
    int h = hash(name);
    int i = index(h);
    add0(h, i, name, strVal);
    return this;
  }

  @Override
  public MultiMap add(CharSequence name, CharSequence value) {
    return add(name.toString(), value.toString());
  }

  @Override
  public MultiMap add(String name, Iterable<String> values) {
    Objects.requireNonNull(name);
    int h = hash(name);
    int i = index(h);

    for (String strVal : values) {
      Objects.requireNonNull(strVal);
      add0(h, i, name, strVal);
    }
    return this;
  }

  @Override
  public MultiMap add(CharSequence name, Iterable<CharSequence> values) {
    Objects.requireNonNull(name);
    String key = name.toString();
    int h = hash(key);
    int i = index(h);

    for (CharSequence cs : values) {
      add0(h, i, key, cs.toString());
    }

    return this;
  }

  @Override
  public MultiMap addAll(MultiMap map) {
    for (Map.Entry<String, String> entry : map.entries()) {
      add(entry.getKey(), entry.getValue());
    }
    return this;
  }

  @Override
  public MultiMap addAll(Map<String, String> headers) {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
    return this;
  }

  private void add0(int h, int i, String name, String v) {
    MapEntry e = entries[i];
    MapEntry newEntry = new MapEntry(h, name, v);
    entries[i] = newEntry;
    newEntry.next = e;

    newEntry.addBefore(head);
  }


  private void remove0(int h, int i, String name) {
    MapEntry pre = null, e = entries[i];
    while (e != null) {
      if (e.hash == h && eq(name, e.key)) {
        e.remove();
        if (pre == null) {
          entries[i] = e.next;
        } else {
          pre.next = e.next;
        }
      } else {
        pre = e;
      }
      e = e.next;
    }
  }

  private boolean eq(String name1, String name2) {
    int nameLen = name1.length();
    if (nameLen != name2.length()) {
      return false;
    }

    for (int i = nameLen - 1; i >= 0; i--) {
      char c1 = name1.charAt(i);
      char c2 = name2.charAt(i);
      if (c1 != c2) {
        if (c1 >= 'A' && c1 <= 'Z') {
          c1 += 32;
        }
        if (c2 >= 'A' && c2 <= 'Z') {
          c2 += 32;
        }
        if (c1 != c2) {
          return false;
        }
      }
    }
    return true;
  }

  private int index(int h) {
    return h % BUCKET_SIZE;
  }

  private static int hash(String name) {
    int h = 0;
    for (int i = name.length() - 1; i >= 0; i--) {
      char c = name.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        c += 32;
      }
      h = 31 * h + c;
    }

    if (h > 0) {
      return h;
    } else if (h == Integer.MIN_VALUE) {
      return Integer.MAX_VALUE;
    } else {
      return -h;
    }
  }

  @Override
  public MultiMap set(String name, String strVal) {
    int h = hash(name);
    int i = index(h);
    remove0(h, i, name);
    add0(h, i, name, strVal);
    return this;
  }

  @Override
  public MultiMap set(CharSequence name, CharSequence value) {
    return set(name.toString(), value.toString());
  }

  @Override
  public MultiMap set(String name, Iterable<String> values) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(values, "values");

    int h = hash(name);
    int i = index(h);

    remove0(h, i, name);
    for (String v : values) {
      if (v != null) {
        add0(h, i, name, v);
      }
    }

    return this;
  }

  @Override
  public MultiMap set(CharSequence name, Iterable<CharSequence> values) {
    remove(name);
    String n = name.toString();
    for (CharSequence seq : values) {
      add(n, seq.toString());
    }
    return this;
  }

  @Override
  public MultiMap setAll(MultiMap map) {
    clear();
    addAll(map);
    return this;
  }

  @Override
  public MultiMap setAll(Map<String, String> headers) {
    clear();
    addAll(headers);
    return this;
  }

  @Override
  public MultiMap remove(String name) {
    Objects.requireNonNull(name);
    int h = hash(name);
    int i = index(h);
    remove0(h, i, name);
    return this;
  }

  @Override
  public MultiMap remove(CharSequence name) {
    return remove(name.toString());
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
    return entries().iterator();
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
    MapEntry next;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : this) {
      sb.append(entry).append('\n');
    }
    return sb.toString();
  }
}
