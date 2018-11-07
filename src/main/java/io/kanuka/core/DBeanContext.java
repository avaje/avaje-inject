package io.kanuka.core;

import io.kanuka.BeanContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DBeanContext implements BeanContext {

  private final String name;

  private final List<BeanLifeCycle> lifeCycleList;

  private final Map<String, DContextEntry> beans;

  private final Map<String, BeanContext> children;

  DBeanContext(String name, List<BeanLifeCycle> lifeCycleList, Map<String, DContextEntry> beans, Map<String, BeanContext> children) {
    this.name = name;
    this.lifeCycleList = lifeCycleList;
    this.beans = beans;
    this.children = children;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public <T> T getBean(Class<T> beanClass) {
    return getBean(beanClass, null);
  }

  @Override
  public <T> T getBean(Class<T> beanClass, String name) {
    DContextEntry entry = beans.get(beanClass.getCanonicalName());
    if (entry != null) {
      T bean = (T) entry.get(name);
      if (bean != null) {
        return bean;
      }
    }
    for (BeanContext childContext : children.values()) {
      T bean = childContext.getBean(beanClass);
      if (bean != null) {
        return bean;
      }
    }
    return null;
  }

  @Override
  public List<Object> getBeans(Class<?> annCls) {

    List<Object> list = new ArrayList<>();

    DContextEntry entry = beans.get(annCls.getCanonicalName());
    if (entry != null) {
      entry.addAll(list);
    }
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeans(annCls));
    }

    return list;
  }
}
