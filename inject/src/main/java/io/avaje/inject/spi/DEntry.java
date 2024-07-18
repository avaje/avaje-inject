package io.avaje.inject.spi;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import io.avaje.inject.BeanEntry;

@NullMarked
final class DEntry implements BeanEntry {

  private final String qualifierName;
  private final int priority;
  private final Object bean;
  private final Set<String> keys = new LinkedHashSet<>();

  DEntry(String qualifierName, int priority, Object bean) {
    this.qualifierName = qualifierName;
    this.priority = priority;
    this.bean = bean;
  }

  @Override
  public String toString() {
    return "{bean=" + bean
      + (qualifierName == null ? "" : ", name='" + qualifierName + '\'')
      + (keys.isEmpty() ? "" : ", keys=" + keys)
      + ", priority=" + priority
      + '}';
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
