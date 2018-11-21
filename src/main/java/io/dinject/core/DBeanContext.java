package io.dinject.core;

import io.dinject.BeanContext;
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

  private final DBeanMap beans;

  private final Map<String, BeanContext> children;

  private boolean closed;

  DBeanContext(String name, String[] dependsOn, List<BeanLifecycle> lifecycleList, DBeanMap beans, Map<String, BeanContext> children) {
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
    T bean = beans.getBean(beanClass, name);
    if (bean != null) {
      return bean;
    }
    for (BeanContext childContext : children.values()) {
      bean = childContext.getBean(beanClass, name);
      if (bean != null) {
        return bean;
      }
    }
    return null;
  }

  @Override
  public <T> List<T> getBeans(Class<T> interfaceType) {
    List<T> list = new ArrayList<>();
    beans.addAll(interfaceType, list);
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeans(interfaceType));
    }
    return list;
  }

  @Override
  public List<Object> getBeansWithAnnotation(Class<?> annotation) {

    List<Object> list = new ArrayList<>();
    beans.addAll(annotation, list);
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeansWithAnnotation(annotation));
    }
    return list;
  }

  @Override
  public void start() {
    synchronized (this) {
      if (name != null) {
        log.debug("firing postConstruct on beans in context:{}", name);
      }
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
        if (name != null) {
          log.debug("firing preDestroy on beans in context:{}", name);
        }
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
