package com.scaiz.vertx.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceHelper {

  public static <T> T loadFactory(Class<T> clazz) {
    T factory = loadFactoryOrNull(clazz);
    if (clazz == null) {
      throw new IllegalStateException("load class " + clazz.getName() + " error");
    }
    return factory;
  }

  public static <T> T loadFactoryOrNull(Class<T> clazz) {
    Collection<T> collection = loadFactories(clazz);
    if (!collection.isEmpty()) {
      return collection.iterator().next();
    } else {
      return null;
    }
  }

  private static <T> Collection<T> loadFactories(Class<T> clazz) {
    return loadFactories(clazz, null);
  }

  private static <T> Collection<T> loadFactories(Class<T> clazz, ClassLoader classLoader) {
    ServiceLoader<T> factories;

    if (classLoader != null) {
      factories = ServiceLoader.load(clazz, classLoader);
    } else {
      factories = ServiceLoader.load(clazz);
      if (!factories.iterator().hasNext()) {
        factories = ServiceLoader.load(clazz, ServiceLoader.class.getClassLoader());
      }
    }

    if (factories.iterator().hasNext()) {
      List<T> list = new ArrayList<>();
      factories.iterator().forEachRemaining(list::add);
      return list;
    }

    return Collections.emptyList();
  }
}
