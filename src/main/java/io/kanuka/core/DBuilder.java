package io.kanuka.core;

import io.kanuka.BeanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

class DBuilder implements Builder {

  private static final Logger log = LoggerFactory.getLogger(DBuilder.class);

  private final List<BeanLifecycle> lifecycleList = new ArrayList<>();

  private final List<Consumer<Builder>> injectors = new ArrayList<>();

  private final Map<String, DContextEntry> beans = new LinkedHashMap<>();

  private final String name;

  private final String[] dependsOn;

  private final Map<String, BeanContext> children = new LinkedHashMap<>();

  private String currentBean;

  private Builder parent;

  /**
   * Create a named context.
   */
  DBuilder(String name, String[] dependsOn) {
    this.name = name;
    this.dependsOn = dependsOn;
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
  public void setParent(Builder parent) {
    this.parent = parent;
  }

  @SuppressWarnings("unchecked")
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
  public void addLifecycle(BeanLifecycle wrapper) {
    lifecycleList.add(wrapper);
  }

  @Override
  public void addInjector(Consumer<Builder> injector) {
    injectors.add(injector);
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

  private void runInjectors() {
    log.debug("perform field injection in context:{}", name);
    for (Consumer<Builder> injector : injectors) {
      injector.accept(this);
    }
  }

  public BeanContext build() {
    runInjectors();
    return new DBeanContext(name, dependsOn, lifecycleList, beans, children);
  }
}
