package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import io.avaje.lang.NonNullApi;

import java.util.LinkedHashSet;
import java.util.Set;

@NonNullApi
class DEntry implements BeanEntry {

  private final String qualifierName;
  private final int priority;
  private final Object bean;
  private final Set<String> keys = new LinkedHashSet<>();

  DEntry(String qualifierName, int priority, Object bean) {
    this.qualifierName = qualifierName;
    this.priority = priority;
    this.bean = bean;
  }

  void addKey(String key) {
    keys.add(key);
  }

  @Override
  public int priority() {
    return priority;
  }

  @Override
  public Object bean() {
    return bean;
  }

  @Override
  public Class<?> type() {
    return bean.getClass();
  }

  @Override
  public String qualifierName() {
    return qualifierName;
  }

  @Override
  public Set<String> keys() {
    return keys;
  }

  @Override
  public boolean hasKey(Class<?> type) {
    return keys.contains(type.getTypeName());
  }
}
