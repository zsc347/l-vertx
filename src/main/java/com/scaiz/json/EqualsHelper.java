package com.scaiz.json;

import java.util.List;
import java.util.Map;

class EqualsHelper {

  static boolean objectEquals(Map<String, Object> m1, Object o) {
    Map m2;
    if (o instanceof JsonObject) {
      m2 = ((JsonObject) o).getMap();
    } else if (o instanceof Map) {
      m2 = (Map) o;
    } else {
      return false;
    }

    if (m1.size() != m2.size()) {
      return false;
    }
    for (Map.Entry entry : m1.entrySet()) {
      Object val = entry.getValue();
      if (val == null) {
        if (m2.get(entry.getKey()) != null) {
          return false;
        }
      } else {
        if (!equals(val, m2.get(entry.getKey()))) {
          return false;
        }
      }
    }
    return true;
  }

  static boolean arrayEquals(List l1, Object o) {
    List l2;
    if (o instanceof List) {
      l2 = (List) o;
    } else if (o instanceof JsonArray) {
      l2 = ((JsonArray) o).getList();
    } else {
      return false;
    }
    if (l1.size() != l2.size()) {
      return false;
    }

    for (int i = 0; i < l1.size(); i++) {
      if (l1.get(i) == null) {
        if (l2.get(i) != null) {
          return false;
        }
      } else {
        if (!equals(l1.get(i), l2.get(i))) {
          return false;
        }
      }
    }

    return true;
  }

  static boolean equals(Object o1, Object o2) {
    if (o1 == o2) {
      return true;
    }

    if (o1 instanceof JsonObject) {
      return objectEquals(((JsonObject) o1).map, o2);
    }

    if (o1 instanceof JsonArray) {
      return arrayEquals((((JsonArray) o1).getList()), o2);
    }

    if (o1 instanceof List<?>) {
      return arrayEquals((List) o1, o2);
    }

    if (o1 instanceof Number && o2 instanceof Number && o1.getClass() != o2
        .getClass()) {
      if (o1 instanceof Float || o2 instanceof Float || o1 instanceof Double
          || o2 instanceof Double) {
        return ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
      } else {
        return ((Number) o1).longValue() == ((Number) o2).longValue();
      }
    }

    return o1.equals(o2);
  }
}
