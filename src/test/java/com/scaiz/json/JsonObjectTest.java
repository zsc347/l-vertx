package com.scaiz.json;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;

public class JsonObjectTest {
  private JsonObject jsonObject;


  @Before
  public void setUp() {
    jsonObject = new JsonObject();
  }

  @Test
  public void testGetNumber() {
    jsonObject.put("foo", 123);
    assertEquals(Long.valueOf(123),
        jsonObject.getLong("foo"));

    jsonObject.put("bar", "hello");
    try {
      jsonObject.getLong("bar");
      fail();
    } catch (ClassCastException e) {
      // Ok
    }
    jsonObject.put("foo", 123L);
    assertEquals(Integer.valueOf(123), jsonObject.getInteger("foo"));
    jsonObject.put("foo", 123d);
    assertEquals(Integer.valueOf(123), jsonObject.getInteger("foo"));
    jsonObject.put("foo", 123f);
    assertEquals(Integer.valueOf(123), jsonObject.getInteger("foo"));
    jsonObject.put("foo", Long.MAX_VALUE);
    assertEquals(Integer.valueOf(-1), jsonObject.getInteger("foo"));

    // Null and absent values
    jsonObject.putNull("foo");
    assertNull(jsonObject.getInteger("foo"));
    assertNull(jsonObject.getInteger("absent"));

    try {
      jsonObject.getInteger(null);
      fail();
    } catch (NullPointerException e) {
      // OK
    }
  }


  @Test
  public void testRemove() {
    jsonObject.put("mystr", "bar");
    jsonObject.put("myint", 123);
    assertEquals("bar", jsonObject.remove("mystr"));
    assertNull(jsonObject.getValue("mystr"));
    assertEquals(123, jsonObject.remove("myint"));
    assertNull(jsonObject.getValue("myint"));
    assertTrue(jsonObject.isEmpty());
  }

  @Test
  public void testIterator() {
    jsonObject.put("foo", "bar");
    jsonObject.put("quux", 123);
    JsonObject obj = createJsonObject();
    jsonObject.put("wibble", obj);
    Iterator<Map.Entry<String, Object>> iter = jsonObject.iterator();
    assertTrue(iter.hasNext());
    Map.Entry<String, Object> entry = iter.next();
    assertEquals("foo", entry.getKey());
    assertEquals("bar", entry.getValue());
    assertTrue(iter.hasNext());
    entry = iter.next();
    assertEquals("quux", entry.getKey());
    assertEquals(123, entry.getValue());
    assertTrue(iter.hasNext());
    entry = iter.next();
    assertEquals("wibble", entry.getKey());
    assertEquals(obj, entry.getValue());
    assertFalse(iter.hasNext());
    iter.remove();
    assertFalse(obj.containsKey("wibble"));
    assertEquals(2, jsonObject.size());
  }

  @Test
  public void testIteratorDoesntChangeObject() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("nestedMap", new HashMap<>());
    map.put("nestedList", new ArrayList<>());
    JsonObject obj = new JsonObject(map);
    Iterator<Map.Entry<String, Object>> iter = obj.iterator();
    Map.Entry<String, Object> entry1 = iter.next();
    assertEquals("nestedMap", entry1.getKey());
    Object val1 = entry1.getValue();
    assertTrue(val1 instanceof JsonObject);
    Map.Entry<String, Object> entry2 = iter.next();
    assertEquals("nestedList", entry2.getKey());
    Object val2 = entry2.getValue();
    assertTrue(val2 instanceof JsonArray);
    assertTrue(map.get("nestedMap") instanceof HashMap);
    assertTrue(map.get("nestedList") instanceof ArrayList);
  }

  @Test
  public void testStream() {
    jsonObject.put("foo", "bar");
    jsonObject.put("quux", 123);

    Iterator<Entry<String, Object>> iter = jsonObject.iterator();
    assertTrue(iter.hasNext());
    Map.Entry<String, Object> entry = iter.next();
    assertEquals("foo", entry.getKey());
    assertEquals("bar", entry.getValue());
    assertTrue(iter.hasNext());
    entry = iter.next();
    assertEquals("quux", entry.getKey());
    assertEquals(123, entry.getValue());
  }

  private JsonObject createJsonObject() {
    JsonObject obj = new JsonObject();
    obj.put("mystr", "bar");
    obj.put("myint", Integer.MAX_VALUE);
    obj.put("mylong", Long.MAX_VALUE);
    obj.put("myfloat", Float.MAX_VALUE);
    obj.put("mydouble", Double.MAX_VALUE);
    obj.put("myboolean", true);
    return obj;
  }

}
