package io.kanuka.core;

import io.kanuka.BeanContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class DBuilder implements BeanLifeCycle, Builder {

  private final List<BeanLifeCycle> lifeCycleList = new ArrayList<>();

  private final Map<String, DContextEntry> beans = new LinkedHashMap<>();

  private final String name;

  private final Map<String, BeanContext> children = new LinkedHashMap<>();

  private String currentBean;

  private Builder parent;

  /**
   * Create a named context.
   */
  DBuilder(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setParent(Builder parent) {
    this.parent = parent;
  }

  @Override
  public void postConstruct() {
    for (BeanLifeCycle lifeCycle : lifeCycleList) {
      lifeCycle.postConstruct();
    }
    for (BeanContext childContext : children.values()) {
      if (childContext instanceof BeanLifeCycle) {
        ((BeanLifeCycle) childContext).postConstruct();
      }
    }
  }

  @Override
  public void preDestroy() {
    for (BeanLifeCycle lifeCycle : lifeCycleList) {
      lifeCycle.preDestroy();
    }
    for (BeanContext childContext : children.values()) {
      if (childContext instanceof BeanLifeCycle) {
        ((BeanLifeCycle) childContext).preDestroy();
      }
    }
  }

  private <T> T getBean(Class<T> beanClass, String name) {

    // look locally first
    DContextEntry entry = beans.get(beanClass.getCanonicalName());
    if (entry != null) {
      T bean = (T) entry.get(name);
      if (bean != null) {
        return bean;
      }
    }

    if (parent != null) {
      // look in parent context (cross-module dependency)
      return parent.get(beanClass, name);
    }

    // look in child context
    for (BeanContext childContext : children.values()) {
      T bean = childContext.getBean(beanClass);
      if (bean != null) {
        return bean;
      }
    }

    return null;
  }

  @Override
  public void addChild(BeanContext child) {
    children.put(child.getName(), child);
  }

  @Override
  public void addBean(Object bean, String name, String... interfaceClass) {
    DContextEntryBean entryBean = new DContextEntryBean(bean, name);
    beans.computeIfAbsent(bean.getClass().getCanonicalName(), s -> new DContextEntry()).add(entryBean);

    if (interfaceClass != null) {
      for (String aClass : interfaceClass) {
        beans.computeIfAbsent(aClass, s -> new DContextEntry()).add(entryBean);
      }
    }
  }

  @Override
  public void addLifecycle(BeanLifeCycle wrapper) {
    lifeCycleList.add(wrapper);
  }


  @Override
  public void currentBean(String currentBean) {
    this.currentBean = currentBean;
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls) {
    return getOptional(cls, null);

  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls, String name) {
    T bean = getBean(cls, name);
    return Optional.ofNullable(bean);
  }

  @Override
  public <T> T get(Class<T> cls) {
    return get(cls, null);
  }

  @Override
  public <T> T get(Class<T> cls, String name) {
    T bean = getBean(cls, name);
    if (bean == null) {
      throw new IllegalStateException("Injecting null for " + cls.getName() + " name:" + name + " when creating " + currentBean);
    }
    return bean;
  }

  public BeanContext build() {
    return new DBeanContext(name, lifeCycleList, beans, children);
  }
}
