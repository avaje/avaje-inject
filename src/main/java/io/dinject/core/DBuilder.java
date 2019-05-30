package io.dinject.core;

import io.dinject.BeanContext;
import io.dinject.BeanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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
  private final boolean hasSuppliedBeans;

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

  private final Map<Class<?>, SpyConsumer> spyMap;

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
    this.hasSuppliedBeans = false;
    this.spyMap = null;
  }

  /**
   * Create for the root builder with supplied beans (test doubles).
   */
  DBuilder(List<SuppliedBean> suppliedBeans, List<SpyConsumer> spyConsumers) {
    this.name = null;
    this.provides = null;
    this.dependsOn = null;
    this.hasSuppliedBeans = (suppliedBeans != null && !suppliedBeans.isEmpty());
    if (hasSuppliedBeans) {
      beanMap.add(suppliedBeans);
    }
    if (spyConsumers == null || spyConsumers.isEmpty()) {
      spyMap = null;
    } else {
      spyMap = new HashMap<>();
      for (SpyConsumer spy : spyConsumers) {
        spyMap.put(spy.getType(), spy);
      }
    }
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
    if (hasSuppliedBeans) {
      return !beanMap.isSupplied(addForType.getName());
    }
    if (parent == null) {
      return true;
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
  public <T> BeanEntry<T> candidate(Class<T> cls, String name) {

    DBeanContext.EntrySort<T> entrySort = new DBeanContext.EntrySort<>();
    entrySort.add(beanMap.candidate(cls, name));
    for (BeanContext childContext : children.values()) {
      entrySort.add(childContext.candidate(cls, name));
    }

    if (parent != null) {
      // look in parent context (cross-module dependency)
      entrySort.add(parent.candidate(cls, name));
    }
    return entrySort.get();
  }

  private <T> T getMaybe(Class<T> beanClass, String name) {
    BeanEntry<T> entry = candidate(beanClass, name);
    return (entry == null) ? null : entry.getBean();
  }

  @Override
  public void addChild(BeanContext child) {
    children.put(child.getName(), child);
  }

  @Override
  public void register(Object bean, String name, Class<?>... types) {
    if (parent != null) {
      // spy consumers only exist on top level builder
      bean = parent.spy(bean, types);
    } else {
      bean = spy(bean, types);
    }
    beanMap.register(bean, name, types);
  }

  /**
   * Return the bean to register potentially with spy enhancement.
   */
  @Override
  public Object spy(Object bean, Class<?>[] types) {
    if (spyMap != null) {
      SpyConsumer spyConsumer = spyMap.get(typeOf(bean, types));
      if (spyConsumer != null) {
        // enrich/enhance the bean for spying
        return spyConsumer.spy(bean);
      }
    }
    return bean;
  }

  /**
   * Return the type to lookup for spy.
   */
  private Class<?> typeOf(Object bean, Class<?>... types) {
    if (types.length > 0) {
      return types[0];
    }
    return bean.getClass();
  }

  @Override
  public void registerPrimary(Object bean, String name, Class<?>... types) {
    beanMap.registerPrimary(bean, name, types);
  }

  @Override
  public void registerSecondary(Object bean, String name, Class<?>... types) {
    beanMap.registerSecondary(bean, name, types);
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
      List<T> beanList = getList(cls);
      msg += " when creating " + injectTarget + " - potential beans to inject: " + beanList;
      if (!beanList.isEmpty()) {
        msg += ". Check @Named or Qualifier being used";
      }
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
