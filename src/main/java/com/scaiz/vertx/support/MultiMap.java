package com.scaiz.vertx.support;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A MultiMap of String keys to a List of String values.
 */
public interface MultiMap extends Iterable<Map.Entry<String, String>> {

  String get(CharSequence name);

  String get(String name);

  List<String> getAll(String name);

  List<String> getAll(CharSequence name);

  List<Entry<String, String>> entries();

  boolean contains(String name);

  boolean contains(CharSequence name);

  default boolean contains(String name, String value, boolean caseInsensitive) {
    return getAll(name).stream()
        .anyMatch(val -> caseInsensitive ? val.equalsIgnoreCase(value)
            : val.equals(value));
  }

  default boolean contains(CharSequence name, CharSequence value,
      boolean caseInsensitive) {
    Predicate<String> predicate;
    if (caseInsensitive) {
      String valueAsString = value.toString();
      predicate = val -> val.equalsIgnoreCase(valueAsString);
    } else {
      predicate = val -> val.contentEquals(value);
    }
    return getAll(name).stream().anyMatch(predicate);
  }

  boolean isEmpty();

  Set<String> names();

  MultiMap add(String name, String value);

  MultiMap add(CharSequence name, CharSequence value);

  MultiMap add(String name, Iterable<String> values);

  MultiMap add(CharSequence name, Iterable<CharSequence> values);

  MultiMap addAll(MultiMap map);

  MultiMap addAll(Map<String, String> headers);

  MultiMap set(String name, String value);

  MultiMap set(CharSequence name, CharSequence value);

  MultiMap set(String name, Iterable<String> values);

  MultiMap set(CharSequence name, Iterable<CharSequence> values);

  MultiMap setAll(MultiMap map);

  MultiMap setAll(Map<String, String> headers);

  MultiMap remove(String name);

  MultiMap remove(CharSequence name);

  MultiMap clear();

  int size();
}
