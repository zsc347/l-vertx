package com.scaiz.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class JsonArrayTest {

  private JsonObject jsonObject;


  @Before
  public void setUp() {
    jsonObject = new JsonObject();
  }


  @Test
  public void testGetNumber() {
    jsonObject.put("foo", 123);
    assertEquals(Long.valueOf(123),
        (Long) jsonObject.getNumber("foo").longValue());

    jsonObject.put("bar", "hello");
    try {
      jsonObject.getNumber("bar");
      fail();
    } catch (ClassCastException e) {
      // Ok
    }
  }
}
