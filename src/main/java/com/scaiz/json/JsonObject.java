package com.scaiz.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonObject {

  private Map<String, Object> map;

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

  public <T> T convertTo(Class<T> type) {
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
    if (number == null) {
      return null;
    } else if (number instanceof Integer) {
      return (Integer) number;
    } else {
      return number.intValue();
    }
  }

  public Long getLong(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    if (number == null) {
      return null;
    } else if (number instanceof Long) {
      return (Long) number;
    } else {
      return number.longValue();
    }
  }

  public Double getDouble(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    if (number == null) {
      return null;
    } else if (number instanceof Double) {
      return (Double) number;
    } else {
      return number.doubleValue();
    }
  }

  public Float getFloat(String key) {
    Objects.requireNonNull(key);
    Number number = (Number) map.get(key);
    if (number == null) {
      return null;
    } else if (number instanceof Float) {
      return (Float) number;
    } else {
      return number.floatValue();
    }
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

}
