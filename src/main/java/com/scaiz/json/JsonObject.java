package com.scaiz.json;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JsonObject implements Iterable<Map.Entry<String, Object>> {

  Map<String, Object> map;

  public JsonObject() {
    map = new LinkedHashMap<>();
  }

  public JsonObject(Map<String, Object> map) {
    this.map = map;
  }

  public Map<String, Object> getMap() {
    return map;
  }

  @SuppressWarnings("unchecked")
  public static JsonObject mapFrom(Object obj) {
    return new JsonObject((Map<String, Object>)
        Json.mapper.convertValue(obj, Map.class));
  }

  public <T> T mapTo(Class<T> type) {
    return Json.mapper.convertValue(map, type);
  }

  public String getString(String key) {
    Objects.requireNonNull(key);
    CharSequence cs = (CharSequence) map.get(key);
    return cs.toString();
  }

  public Integer getInteger(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    return NumberHelper.toInteger(number);
  }

  public Long getLong(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    return NumberHelper.toLong(number);
  }


  public Double getDouble(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    return NumberHelper.toDouble(number);
  }

  public Float getFloat(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    return NumberHelper.toFloat(number);
  }


  public Boolean getBoolean(String key) {
    Objects.requireNonNull(key);
    return (Boolean) map.get(key);
  }

  public JsonObject getJsonObject(String key) {
    Objects.requireNonNull(key);
    Object val = map.get(key);
    if (val instanceof Map) {
      val = new JsonObject((Map) val);
    }
    return (JsonObject) val;
  }

  public JsonArray getJsonArray(String key) {
    Objects.requireNonNull(key);
    Object val = map.get(key);
    if (val instanceof List) {
      val = new JsonArray((List) val);
    }
    return (JsonArray) val;
  }

  public Object getValue(String key) {
    Objects.requireNonNull(key);
    Object val = map.get(key);
    if (val instanceof Map) {
      return new JsonObject((Map) val);
    } else if (val instanceof List) {
      return new JsonArray((List) val);
    }
    return val;
  }

  public boolean containsKey(String key) {
    Objects.requireNonNull(key);
    return map.containsKey(key);
  }

  public Set<String> fields() {
    return map.keySet();
  }

  public JsonObject put(String key, Enum val) {
    Objects.requireNonNull(val);
    map.put(key, val == null ? null : val.name());
    return this;
  }

  public JsonObject put(String key, CharSequence value) {
    Objects.requireNonNull(key);
    map.put(key, value == null ? null : value.toString());
    return this;
  }

  public JsonObject put(String key, String value) {
    Objects.requireNonNull(key);
    map.put(key, value);
    return this;
  }

  public JsonObject put(String key, Number value) {
    Objects.requireNonNull(key);
    map.put(key, value);
    return this;
  }

  public JsonObject put(String key, Boolean value) {
    Objects.requireNonNull(key);
    map.put(key, value);
    return this;
  }

  public JsonObject put(String key, JsonObject value) {
    Objects.requireNonNull(key);
    map.put(key, value);
    return this;
  }

  public JsonObject put(String key, JsonArray value) {
    Objects.requireNonNull(key);
    map.put(key, value);
    return this;
  }

  public Object remove(String key) {
    return map.remove(key);
  }

  public String encode() {
    return Json.encode(map);
  }


  public String encodePrettily() {
    return Json.encodePrettily(map);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return EqualsHelper.objectEquals(map, o);

  }


  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public Iterator<Map.Entry<String, Object>> iterator() {
    return new Iter(map.entrySet().iterator());
  }

  public JsonObject putNull(String key) {
    Objects.requireNonNull(key);
    map.put(key, null);
    return this;
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public int size() {
    return map.size();
  }


  private class Iter implements Iterator<Map.Entry<String, Object>> {

    private Iterator<Map.Entry<String, Object>> mapIter;

    Iter(Iterator<Map.Entry<String, Object>> mapIter) {
      this.mapIter = mapIter;
    }

    @Override
    public boolean hasNext() {
      return mapIter.hasNext();
    }

    @Override
    public Entry next() {
      Map.Entry<String, Object> mapEntry = mapIter.next();
      if (mapEntry.getValue() instanceof Map) {
        return new Entry(mapEntry.getKey(),
            new JsonObject((Map) mapEntry.getValue()));
      } else if (mapEntry.getValue() instanceof List) {
        return new Entry(mapEntry.getKey(),
            new JsonArray((List) mapEntry.getValue()));
      }
      return new Entry(mapEntry.getKey(), mapEntry.getValue());
    }

    @Override
    public void remove() {
      mapIter.remove();
    }

  }

  private class Entry implements Map.Entry<String, Object> {

    final String key;
    final Object value;

    Entry(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public String getKey() {
      return this.key;
    }

    @Override
    public Object getValue() {
      return this.value;
    }

    @Override
    public Object setValue(Object value) {
      throw new UnsupportedOperationException();
    }
  }
}
