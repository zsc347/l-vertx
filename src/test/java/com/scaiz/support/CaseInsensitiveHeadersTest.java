package com.scaiz.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseInsensitiveHeadersTest {

  private MultiMap newMultiMap() {
    return new CaseInsensitiveHeaders();
  }

  @Test
  public void testCaseInsensitiveHeaders() {

    MultiMap result = newMultiMap();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
    assertEquals("", result.toString());
  }

  @Test
  public void testAddTest1() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> map = new HashMap<>();
    map.put("a", "b");

    MultiMap result = mmap.addAll(map);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("a: b\n", result.toString());
  }

  @Test
  public void testAddTest2() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", "d");

    assertEquals("a: b\nc: d\n", mmap.addAll(map).toString());
  }

  @Test
  public void testAddTest3() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> map = new HashMap<>();
    map.put("a", "b");

    assertEquals("a: b\n", mmap.addAll(map).toString());
  }

  @Test
  public void testAddTest4() {
    MultiMap mmap = newMultiMap();
    Map<String, String> map = new HashMap<>();

    assertEquals("", mmap.addAll(map).toString());
  }

  @Test
  public void testAddTest5() {
    MultiMap mmap = newMultiMap();
    MultiMap headers = newMultiMap();

    assertEquals("", mmap.addAll(headers).toString());
  }

  @Test
  public void testAddTest7() {
    MultiMap mmap = newMultiMap();
    CharSequence name = "name";
    CharSequence value = "value";

    assertEquals("name: value\n", mmap.add(name, value).toString());
  }

  @Test
  public void testAddTest8() {
    MultiMap mmap = newMultiMap();
    CharSequence name = "name";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals("name: somevalue\n", mmap.add(name, values).toString());
  }

  @Test
  public void testAddTest9() {
    MultiMap mmap = newMultiMap();
    String name = "";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals(": somevalue\n", mmap.add(name, values).toString());
  }

  @Test
  public void testAddTest10() {
    MultiMap mmap = newMultiMap();
    String name = "a";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals("a: somevalue\n", mmap.add(name, values).toString());
  }

  @Test
  public void testAddTest11() {
    MultiMap mmap = newMultiMap();
    String name = "";
    String strVal = "";

    assertEquals(": \n", mmap.add(name, strVal).toString());
  }

  @Test
  public void testAddTest12() {
    MultiMap mmap = newMultiMap();
    String name = "a";
    String strVal = "b";

    assertEquals("a: b\n", mmap.add(name, strVal).toString());
  }

  @Test
  public void testAddTest13() {
    MultiMap mmap = newMultiMap();
    String name = "aaa";
    String strVal = "";

    assertEquals("aaa: \n", mmap.add(name, strVal).toString());
  }

  @Test
  public void testAddTest14() {
    MultiMap mmap = newMultiMap();
    String name = "";
    String strVal = "aaa";

    assertEquals(": aaa\n", mmap.add(name, strVal).toString());
  }

  @Test
  public void testAddIterable() {
    MultiMap mmap = newMultiMap();
    String name = "name";
    List<String> values = new ArrayList<>();
    values.add("value1");
    values.add("value2");

    MultiMap result = mmap.add(name, values);

    assertEquals(1, result.size());
    assertEquals("name: value1\nname: value2\n", result.toString());
  }

  @Test
  public void testAddMultiMap() {
    MultiMap mmap = newMultiMap();

    MultiMap mm = newMultiMap();
    mm.add("Header1", "value1");
    mm.add("Header2", "value2");

    MultiMap result = mmap.addAll(mm);

    assertEquals(2, result.size());
    assertEquals("Header1: value1\nHeader2: value2\n", result.toString());
  }

  @Test
  public void testClearTest1() {
    MultiMap mmap = newMultiMap();

    MultiMap result = mmap.clear();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
    assertEquals("", result.toString());
  }

  @Test
  public void testContainsTest1() {
    MultiMap mmap = newMultiMap();
    CharSequence name = String.valueOf(new Object());

    assertFalse(mmap.contains(name));
  }

  @Test
  public void testContainsTest2() {
    MultiMap mmap = newMultiMap();
    String name = "";

    assertFalse(mmap.contains(name));
  }

  @Test
  public void testContainsTest3() {
    MultiMap mmap = newMultiMap();
    String name = "0123456789";

    boolean result = mmap.contains(name);

    assertFalse(result);
    mmap.add(name, "");
    result = mmap.contains(name);
    assertTrue(result);
  }

  @Test
  public void testEntriesTest1() {
    MultiMap mmap = newMultiMap();

    List<Map.Entry<String, String>> result = mmap.entries();

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetTest1() {
    MultiMap mmap = newMultiMap();
    CharSequence name = String.valueOf(new Object());

    assertNull(mmap.get(name));
  }

  @Test
  public void testGetTest2() {
    MultiMap mmap = newMultiMap();
    String name = "1";

    assertNull(mmap.get(name));
  }

  @Test
  public void testGetTest3() {
    MultiMap mmap = newMultiMap();
    String name = "name";

    String result = mmap.get(name);
    assertNull(result);
    mmap.add(name, "value");
    result = mmap.get(name);
    assertEquals("value", result);
  }

  @Test(expected = NullPointerException.class)
  public void testGetNPE() {
    new CaseInsensitiveHeaders().get(null);
  }

  @Test
  public void testGetAllTest1() {
    MultiMap mmap = newMultiMap();
    CharSequence name = String.valueOf(new Object());

    List<String> result = mmap.getAll(name);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetAllTest2() {
    MultiMap mmap = newMultiMap();
    String name = "1";

    List<String> result = mmap.getAll(name);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetAllTest3() {
    MultiMap mmap = newMultiMap();
    String name = "name";

    List<String> result = mmap.getAll(name);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetAll() {
    MultiMap mmap = newMultiMap();
    String name = "name";
    mmap.add(name, "value1");
    mmap.add(name, "value2");

    List<String> result = mmap.getAll(name);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("value1", result.get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testGetAllNPE() {
    new CaseInsensitiveHeaders().getAll(null);
  }

  @Test
  public void testIsEmptyTest1() {
    MultiMap mmap = newMultiMap();

    assertTrue(mmap.isEmpty());
  }

  @Test
  public void testIsEmptyTest2() {
    MultiMap mmap = newMultiMap();
    mmap.add("a", "b");

    assertFalse(mmap.isEmpty());
  }

  @Test
  public void testIteratorTest1() {
    MultiMap mmap = newMultiMap();

    Iterator<Entry<String, String>> result = mmap.iterator();

    assertNotNull(result);
    assertFalse(result.hasNext());
  }

  @Test
  public void testIteratorTest2() {
    MultiMap mmap = newMultiMap();
    mmap.add("a", "b");

    Iterator<Map.Entry<String, String>> result = mmap.iterator();

    assertNotNull(result);
    assertTrue(result.hasNext());
  }

  @Test
  public void testNamesTest1() {
    MultiMap mmap = newMultiMap();

    Set<String> result = mmap.names();

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testRemoveTest1() {
    MultiMap mmap = newMultiMap();
    CharSequence name = String.valueOf(new Object());

    MultiMap result = mmap.remove(name);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
  }

  @Test(expected = NullPointerException.class)
  public void testRemoveNPE() {
    new CaseInsensitiveHeaders().remove(null);
  }

  @Test
  public void testRemoveTest2() {
    MultiMap mmap = newMultiMap();
    String name = "1";

    MultiMap result = mmap.remove(name);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
  }

  @Test
  public void testRemoveTest3() {
    MultiMap mmap = newMultiMap();
    String name = "name";

    MultiMap result = mmap.remove(name);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
  }

  @Test
  public void testRemoveTest4() {
    MultiMap mmap = newMultiMap();
    mmap.add("name", "value1");
    mmap.add("name", "value2");

    assertTrue(mmap.contains("name"));
    MultiMap result = mmap.remove("name");
    assertFalse(result.contains("name"));
  }

  @Test
  public void testSetTest1() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> headers = new HashMap<>();
    headers.put("", "");

    MultiMap result = mmap.setAll(headers);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(": \n", result.toString());
  }

  @Test
  public void testSetTest2() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> headers = new HashMap<>();
    headers.put("", "");
    headers.put("aaa", "bbb");

    MultiMap result = mmap.setAll(headers);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertEquals(": \naaa: bbb\n", result.toString());
  }

  @Test
  public void testSetTest3() {
    MultiMap mmap = newMultiMap();
    HashMap<String, String> headers = new HashMap<>();
    headers.put("aaa", "bbb");

    MultiMap result = mmap.setAll(headers);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("aaa: bbb\n", result.toString());
  }

  @Test
  public void testSetTest4() {
    MultiMap mmap = newMultiMap();
    Map<String, String> headers = new HashMap<>();

    MultiMap result = mmap.setAll(headers);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
    assertEquals("", result.toString());
  }

  @Test
  public void testSetTest5() {
    MultiMap mmap = newMultiMap();
    MultiMap headers = newMultiMap();

    MultiMap result = mmap.setAll(headers);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
    assertEquals("", result.toString());
  }

  @Test
  public void testSetTest7() {
    MultiMap mmap = newMultiMap();
    CharSequence name = "name";
    CharSequence value = "value";

    MultiMap result = mmap.set(name, value);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("name: value\n", result.toString());
  }

  @Test
  public void testSetTest8() {
    MultiMap mmap = newMultiMap();
    CharSequence name = "name";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals("name: somevalue\n", mmap.set(name, values).toString());
  }

  @Test
  public void testSetTest9() {
    MultiMap mmap = newMultiMap();
    String name = "";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals(": somevalue\n", mmap.set(name, values).toString());
  }

  @Test
  public void testSetTest10() {
    MultiMap mmap = newMultiMap();
    String name = "aaa";
    ArrayList<CharSequence> values = new ArrayList<>();
    values.add("somevalue");

    assertEquals("aaa: somevalue\n", mmap.set(name, values).toString());
  }

  @Test
  public void testSetTest11() {
    MultiMap mmap = newMultiMap();
    String name = "";
    String strVal = "";

    MultiMap result = mmap.set(name, strVal);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(": \n", result.toString());
  }

  @Test
  public void testSetTest12() {
    MultiMap mmap = newMultiMap();
    String name = "aaa";
    String strVal = "bbb";

    MultiMap result = mmap.set(name, strVal);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("aaa: bbb\n", result.toString());
  }

  @Test
  public void testSetTest13() {
    MultiMap mmap = newMultiMap();
    String name = "aaa";
    String strVal = "";

    MultiMap result = mmap.set(name, strVal);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("aaa: \n", result.toString());
  }

  @Test
  public void testSetTest14() {
    MultiMap mmap = newMultiMap();
    String name = "";
    String strVal = "bbb";

    MultiMap result = mmap.set(name, strVal);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(": bbb\n", result.toString());
  }

  @Test(expected = NullPointerException.class)
  public void testSetIterableNPE() {
    new CaseInsensitiveHeaders().set("name", (Iterable<String>) null);
  }

  @Test
  public void testSetIterableEmpty() {
    MultiMap mmap = newMultiMap();

    String name = "name";
    List<String> values = new ArrayList<>();

    MultiMap result = mmap.set(name, values);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
    assertEquals("", result.toString());
  }

  @Test
  public void testSetIterable() {
    MultiMap mmap = newMultiMap();

    String name = "name";
    List<String> values = new ArrayList<>();
    values.add("value1");
    values.add(null);

    MultiMap result = mmap.set(name, values);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("name: value1\n", result.toString());
  }

  @Test
  public void testSize() {
    MultiMap mmap = newMultiMap();

    assertEquals(0, mmap.size());
    mmap.add("header", "value");
    assertEquals(1, mmap.size());
    mmap.add("header2", "value2");
    assertEquals(2, mmap.size());
    mmap.add("header", "value3");
    assertEquals(2, mmap.size());
  }

  @Test
  public void testGetHashColl() {
    MultiMap mm = newMultiMap();
    String name1 = "!~AZ";
    String name2 = "!~\u0080Y";
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));

    mm = new CaseInsensitiveHeaders();
    name1 = "";
    name2 = "\0";
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));

    mm = new CaseInsensitiveHeaders();
    name1 = "AZa";
    name2 = "\u0080YA";
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));

    mm = new CaseInsensitiveHeaders();
    name1 = " !";
    name2 = "? ";
    assertTrue("hash error", hash(name1) == hash(name2));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));

    mm = new CaseInsensitiveHeaders();
    name1 = "\u0080a";
    name2 = "Ab";
    assertTrue("hash error", hash(name1) == hash(name2));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));

    // same bucket, different hash
    mm = new CaseInsensitiveHeaders();
    name1 = "A";
    name2 = "R";
    assertTrue("hash error", index(hash(name1)) == index(hash(name2)));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("value1", mm.get(name1));
    assertEquals("value2", mm.get(name2));
  }

  @Test
  public void testGetAllHashColl() {
    MultiMap mm = newMultiMap();
    String name1 = "AZ";
    String name2 = "\u0080Y";
    assertTrue("hash error", hash(name1) == hash(name2));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("[value1]", mm.getAll(name1).toString());
    assertEquals("[value2]", mm.getAll(name2).toString());

    mm = new CaseInsensitiveHeaders();
    name1 = "A";
    name2 = "R";
    assertTrue("hash error", index(hash(name1)) == index(hash(name2)));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    assertEquals("[value1]", mm.getAll(name1).toString());
    assertEquals("[value2]", mm.getAll(name2).toString());
  }

  @Test
  public void testRemoveHashColl() {
    MultiMap mm = newMultiMap();
    String name1 = "AZ";
    String name2 = "\u0080Y";
    String name3 = "RZ";
    assertTrue("hash error", hash(name1) == hash(name2));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    mm.add(name3, "value3");
    mm.add(name1, "value4");
    mm.add(name2, "value5");
    mm.add(name3, "value6");
    assertEquals(3, mm.size());
    mm.remove(name1);
    mm.remove(name2);
    assertEquals(1, mm.size());

    mm = new CaseInsensitiveHeaders();
    name1 = "A";
    name2 = "R";
    assertTrue("hash error", index(hash(name1)) == index(hash(name2)));
    mm.add(name1, "value1");
    mm.add(name2, "value2");
    assertEquals(2, mm.size());
    mm.remove(name1);
    mm.remove(name2);
    assertTrue("not empty", mm.isEmpty());
  }

  // hash function copied from method under test
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

  private static int index(int hash) {
    return hash % 17;
  }

  // construct a string with hash==MIN_VALUE
  // to get coverage of the if in hash()
  // we will calculate the representation of
  // MAX_VALUE+1 in base31, which wraps around to
  // MIN_VALUE in int representation
  @Test
  public void testHashMININT() {
    MultiMap mm = newMultiMap();
    StringBuilder name1 = new StringBuilder();
    long value = Integer.MAX_VALUE;
    value++;
    int base = 31;
    long pow = 1;

    while (value > pow * base) {
      pow *= base;
    }

    while (pow != 0) {
      long mul = value / pow;
      name1.insert(0, ((char) mul));
      value -= pow * mul;
      pow /= base;
    }
    name1.insert(0, ((char) value));
    mm.add(name1.toString(), "value");
    assertEquals("value", mm.get(name1.toString()));
  }

  // we have to sort the string since a map doesn't do sorting
  private String sortByLine(String str) {
    String[] lines = str.split("\n");
    Arrays.sort(lines);
    StringBuilder sb = new StringBuilder();
    for (String s : lines) {
      sb.append(s);
      sb.append("\n");
    }
    return sb.toString();
  }

  @Test
  public void testToString() {
    MultiMap mm = newMultiMap();
    assertEquals("", mm.toString());
    mm.add("Header1", "Value1");
    assertEquals("Header1: Value1\n",
      sortByLine(mm.toString()));
    mm.add("Header2", "Value2");
    assertEquals("Header1: Value1\n"
        + "Header2: Value2\n",
      sortByLine(mm.toString()));
    mm.add("Header1", "Value3");
    assertEquals("Header1: Value1\n"
        + "Header1: Value3\n"
        + "Header2: Value2\n",
      sortByLine(mm.toString()));
    mm.remove("Header1");
    assertEquals("Header2: Value2\n",
      sortByLine(mm.toString()));
    mm.set("Header2", "Value4");
    assertEquals("Header2: Value4\n",
      sortByLine(mm.toString()));
  }

  /*
   * unit tests for public method in MapEntry
   * (isn't actually used in the implementation)
   */

  @Test
  public void testMapEntrySetValue() {
    MultiMap mmap = newMultiMap();

    mmap.add("Header", "oldValue");

    for (Map.Entry<String, String> me : mmap) {
      me.setValue("newValue");
    }
    assertEquals("newValue", mmap.get("Header"));
  }

  @Test
  public void testMapEntryToString() {
    MultiMap mmap = newMultiMap();

    mmap.add("Header", "value");

    assertEquals("Header: value", mmap.iterator().next().toString());
  }

  @Test(expected = NullPointerException.class)
  public void testMapEntrySetValueNull() {
    MultiMap mmap = newMultiMap();

    mmap.add("Header", "oldvalue");

    for (Map.Entry<String, String> me : mmap) {
      me.setValue(null);
    }
  }

  @Test
  public void testContainsValueString() {
    MultiMap mmap = newMultiMap();

    mmap.add("headeR", "vAlue");

    assertTrue(mmap.contains("heaDer", "vAlue", false));
    assertFalse(mmap.contains("heaDer", "Value", false));
  }

  @Test
  public void testContainsValueStringIgnoreCase() {
    MultiMap mmap = newMultiMap();

    mmap.add("headeR", "vAlue");

    List<String> l = mmap.getAll("heaDer");
    assertTrue(mmap.contains("heaDer", "vAlue", true));
    assertTrue(mmap.contains("heaDer", "Value", true));
  }
}
