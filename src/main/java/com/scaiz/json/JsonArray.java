package com.scaiz.json;

import com.scaiz.buffer.impl.BufferImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jdk.internal.org.objectweb.asm.TypeReference;


public class JsonArray implements Iterable<Object> {

  private List<Object> list;

  public JsonArray() {
    list = new ArrayList<>();
  }

  public JsonArray(List list) {
    this.list = list;
  }

  public JsonArray(BufferImpl buffer) {
    fromBuffer(buffer);
  }

  @SuppressWarnings("unchecked")
  private void fromBuffer(BufferImpl buffer) {
    list = Json.decode(buffer, List.class);
  }

  public List getList() {
    return list;
  }

  public String getString(int pos) {
    CharSequence cs = (CharSequence) list.get(pos);
    return cs == null ? null : cs.toString();
  }

  public Integer getInteger(int pos) {
    Number number = (Number) list.get(pos);
    return NumberHelper.toInteger(number);
  }

  public Long getLong(int pos) {
    Number number = (Number) list.get(pos);
    return NumberHelper.toLong(number);
  }

  public Float getFloat(int pos) {
    Number number = (Number) list.get(pos);
    return NumberHelper.toFloat(number);
  }

  public Double getDouble(int pos) {
    Number number = (Number) list.get(pos);
    return NumberHelper.toDouble(number);
  }


  public Boolean getBoolean(int pos) {
    return (Boolean) list.get(pos);
  }

  public JsonObject getJsonObject(int pos) {
    Object val = list.get(pos);
    if (val instanceof Map) {
      val = new JsonObject((Map) val);
    }
    return (JsonObject) val;
  }

  public JsonArray getJsonArray(int pos) {
    Object val = list.get(pos);
    if (val instanceof List) {
      val = new JsonArray((List) val);
    }
    return (JsonArray) val;
  }

  public String encode() {
    return Json.encode(list);
  }

  public String encodePrettily() {
    return Json.encodePrettily(list);
  }

  @Override
  public String toString() {
    return encode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return EqualsHelper.arrayEquals(list, o);
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iter(list.iterator());
  }


  private class Iter implements Iterator<Object> {

    final Iterator<Object> iter;

    Iter(Iterator<Object> listIter) {
      this.iter = listIter;
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public Object next() {
      Object val = iter.next();
      if (val instanceof Map) {
        val = new JsonObject((Map) val);
      } else if (val instanceof List) {
        val = new JsonArray((List) val);
      }
      return val;
    }

    @Override
    public void remove() {
      iter.remove();
    }
  }

}