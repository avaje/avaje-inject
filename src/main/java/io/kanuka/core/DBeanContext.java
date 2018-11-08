package io.kanuka.core;

import io.kanuka.BeanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DBeanContext implements BeanContext {

  private static final Logger log = LoggerFactory.getLogger(DBeanContext.class);

  private final String name;

  private final String[] dependsOn;

  private final List<BeanLifecycle> lifecycleList;

  private final Map<String, DContextEntry> beans;

  private final Map<String, BeanContext> children;

  private boolean closed;

  DBeanContext(String name, String[] dependsOn, List<BeanLifecycle> lifecycleList, Map<String, DContextEntry> beans, Map<String, BeanContext> children) {
    this.name = name;
    this.dependsOn = dependsOn;
    this.lifecycleList = lifecycleList;
    this.beans = beans;
    this.children = children;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getDependsOn() {
    return dependsOn;
  }

  @Override
  public <T> T getBean(Class<T> beanClass) {
    return getBean(beanClass, null);
  }

  @SuppressWarnings("unchecked")
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

  @Override
  public void start() {
    synchronized (this) {
      log.debug("firing postConstruct on beans in context:{}", name);
      for (BeanLifecycle bean : lifecycleList) {
        bean.postConstruct();
      }
      for (BeanContext childContext : children.values()) {
        childContext.start();
      }
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!closed) {
        // we only allow one call to preDestroy
        closed = true;
        log.debug("firing preDestroy on beans in context:{}", name);
        for (BeanLifecycle bean : lifecycleList) {
          bean.preDestroy();
        }
        for (BeanContext childContext : children.values()) {
          childContext.close();
        }
      }
    }
  }
}
