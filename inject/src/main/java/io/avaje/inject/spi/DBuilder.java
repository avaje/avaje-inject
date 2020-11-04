package io.avaje.inject.spi;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
  final DBeanMap beanMap = new DBeanMap();

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

  Builder parent;

  /**
   * Create a named context for non-root builders.
   */
  DBuilder(String name, String[] provides, String[] dependsOn) {
    this.name = name;
    this.provides = provides;
    this.dependsOn = dependsOn;
  }

  /**
   * Create for the root builder.
   */
  DBuilder() {
    this.name = null;
    this.provides = null;
    this.dependsOn = null;
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

  @Override
  public <T> Set<T> getSet(Class<T> interfaceType) {
    return new LinkedHashSet<>(getList(interfaceType));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
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
  public void addChild(BeanContextFactory factory) {
    final BeanContext context = factory.createContext(this);
    children.put(context.getName(), context);
  }

  /**
   * Return the bean to register potentially with spy enhancement.
   */
  @Override
  public <T> T enrich(T bean, Class<?>[] types) {
    // only enriched by DBuilderExtn
    return bean;
  }

  @Override
  public <T> T register(T bean, String name, Class<?>... types) {
    String canonicalName = bean.getClass().getCanonicalName();
    if (parent != null) {
      // enrichment only exist on top level builder
      bean = parent.enrich(bean, types);
    }
    beanMap.register(canonicalName, bean, name, types);
    return bean;
  }

  @Override
  public <T> T registerPrimary(T bean, String name, Class<?>... types) {
    String canonicalName = bean.getClass().getCanonicalName();
    if (parent != null) {
      // enrichment only exist on top level builder
      bean = parent.enrich(bean, types);
    }
    beanMap.registerPrimary(canonicalName, bean, name, types);
    return bean;
  }

  @Override
  public <T> T registerSecondary(T bean, String name, Class<?>... types) {
    String canonicalName = bean.getClass().getCanonicalName();
    if (parent != null) {
      // enrichment only exist on top level builder
      bean = parent.enrich(bean, types);
    }
    beanMap.registerSecondary(canonicalName, bean, name, types);
    return bean;
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
