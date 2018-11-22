package io.dinject.core;

import io.dinject.BeanContext;
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

  /**
   * List of Lifecycle beans.
   */
  private final List<BeanLifecycle> lifecycleList = new ArrayList<>();

  /**
   * List of field injection closures.
   */
  private final List<Consumer<Builder>> injectors = new ArrayList<>();

  /**
   * The beans created and added to the context during building.
   */
  private final DBeanMap beanMap = new DBeanMap();

  /**
   * Supplied beans (test doubles) given to the context prior to building.
   */
  private final DBeanMap suppliedBeanMap;

  /**
   * The context/module name.
   */
  private final String name;

  /**
   * The module features this context provides.
   */
  private final String[] provides;

  /**
   * The other modules this context dependsOn (that should be built prior).
   */
  private final String[] dependsOn;

  private final Map<String, BeanContext> children = new LinkedHashMap<>();

  /**
   * Debug of the current bean being wired - used in injection errors.
   */
  private Class<?> injectTarget;

  private Builder parent;

  /**
   * Create a named context for non-root builders.
   */
  DBuilder(String name, String[] provides, String[] dependsOn) {
    this.name = name;
    this.provides = provides;
    this.dependsOn = dependsOn;
    this.suppliedBeanMap = null;
  }

  /**
   * Create for the root builder with supplied beans (test doubles).
   */
  DBuilder(List<Object> suppliedBeans) {
    this.name = null;
    this.provides = null;
    this.dependsOn = null;
    this.suppliedBeanMap = new DBeanMap(suppliedBeans);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getProvides() {
    return provides;
  }

  @Override
  public String[] getDependsOn() {
    return dependsOn;
  }

  @Override
  public void setParent(Builder parent) {
    this.parent = parent;
  }

  @Override
  public boolean isAddBeanFor(Class<?> addForType, Class<?> injectTarget) {
    if (suppliedBeanMap != null) {
      return suppliedBeanMap.isAddBeanFor(addForType.getName());
    }
    this.injectTarget = injectTarget;
    return parent.isAddBeanFor(addForType);
  }

  @Override
  public boolean isAddBeanFor(Class<?> injectTarget) {
    return isAddBeanFor(injectTarget, injectTarget);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> getList(Class<T> interfaceType) {

    List list = new ArrayList<>();
    if (suppliedBeanMap != null) {
      suppliedBeanMap.addAll(interfaceType, list);
      if (!list.isEmpty()) {
        // not sure if this is correct, supplied so skip
        return (List<T>) list;
      }
    }

    beanMap.addAll(interfaceType, list);
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeansWithAnnotation(interfaceType));
    }
    if (parent != null) {
      list.addAll(parent.getList(interfaceType));
    }
    return (List<T>) list;
  }

  @Override
  public <T> T getMaybe(Class<T> beanClass, String name) {

    T bean;
    if (suppliedBeanMap != null) {
      bean = suppliedBeanMap.getBean(beanClass, name);
      if (bean != null) {
        return bean;
      }
    }

    // look locally first
    bean = beanMap.getBean(beanClass, name);
    if (bean != null) {
      return bean;
    }

    // look in child context
    for (BeanContext childContext : children.values()) {
      bean = childContext.getBean(beanClass);
      if (bean != null) {
        return bean;
      }
    }

    if (parent != null) {
      // look in parent context (cross-module dependency)
      return parent.getMaybe(beanClass, name);
    }

    return null;
  }

  @Override
  public void addChild(BeanContext child) {
    children.put(child.getName(), child);
  }

  @Override
  public void register(Object bean, String name, Class<?>... types) {
    beanMap.register(bean, name, types);
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
  public <T> Optional<T> getOptional(Class<T> cls) {
    return getOptional(cls, null);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    return Optional.ofNullable(bean);
  }

  @Override
  public <T> T get(Class<T> cls) {
    return get(cls, null);
  }

  @Override
  public <T> T get(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    if (bean == null) {
      String msg = "Injecting null for " + cls.getName();
      if (name != null) {
        msg += " name:" + name;
      }
      msg += " when creating " + injectTarget;
      throw new IllegalStateException(msg);
    }
    return bean;
  }

  private void runInjectors() {
    if (name != null) {
      log.debug("perform field injection in context:{}", name);
    }
    for (Consumer<Builder> injector : injectors) {
      injector.accept(this);
    }
  }

  public BeanContext build() {
    runInjectors();
    return new DBeanContext(name, provides, dependsOn, lifecycleList, beanMap, children);
  }
}
