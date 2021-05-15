package io.avaje.inject;

import io.avaje.inject.spi.BeanContextFactory;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.EnrichBean;
import io.avaje.inject.spi.SuppliedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Build a bean context with options for shutdown hook and supplying test doubles.
 */
class DBeanScopeBuilder implements BeanScopeBuilder {

  private static final Logger log = LoggerFactory.getLogger(DBeanScopeBuilder.class);

  private boolean shutdownHook = true;

  @SuppressWarnings("rawtypes")
  private final List<SuppliedBean> suppliedBeans = new ArrayList<>();

  @SuppressWarnings("rawtypes")
  private final List<EnrichBean> enrichBeans = new ArrayList<>();

  private final Set<String> includeModules = new LinkedHashSet<>();

  private boolean ignoreMissingModuleDependencies;

  /**
   * Create a BeanContextBuilder to ultimately load and return a new BeanContext.
   */
  DBeanScopeBuilder() {
  }

  @Override
  public BeanScopeBuilder withNoShutdownHook() {
    this.shutdownHook = false;
    return this;
  }

  @Override
  public BeanScopeBuilder withModules(String... modules) {
    this.includeModules.addAll(Arrays.asList(modules));
    return this;
  }

  @Override
  public BeanScopeBuilder withIgnoreMissingModuleDependencies() {
    this.ignoreMissingModuleDependencies = true;
    return this;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public BeanScopeBuilder withBeans(Object... beans) {
    for (Object bean : beans) {
      suppliedBeans.add(new SuppliedBean(null, suppliedType(bean.getClass()), bean));
    }
    return this;
  }

  @Override
  public <D> BeanScopeBuilder withBean(Class<D> type, D bean) {
    return withBean(null, type, bean);
  }

  @Override
  public <D> BeanScopeBuilder withBean(String name, Class<D> type, D bean) {
    suppliedBeans.add(new SuppliedBean<>(name, type, bean));
    return this;
  }

  @Override
  public BeanScopeBuilder withMock(Class<?> type) {
    return withMock(type, null);
  }

  @Override
  public <D> DBeanScopeBuilder withMock(Class<D> type, Consumer<D> consumer) {
    suppliedBeans.add(new SuppliedBean<>(null, type, null, consumer));
    return this;
  }

  @Override
  public BeanScopeBuilder withSpy(Class<?> type) {
    return withSpy(type, null);
  }

  @Override
  public <D> DBeanScopeBuilder withSpy(Class<D> type, Consumer<D> consumer) {
    enrichBeans.add(new EnrichBean<>(type, consumer));
    return this;
  }

  @Override
  public BeanScope build() {
    // sort factories by dependsOn
    FactoryOrder factoryOrder = new FactoryOrder(includeModules, !suppliedBeans.isEmpty(), ignoreMissingModuleDependencies);
    ServiceLoader.load(BeanContextFactory.class).forEach(factoryOrder::add);

    Set<String> moduleNames = factoryOrder.orderFactories();
    if (moduleNames.isEmpty()) {
      throw new IllegalStateException("No modules found. When using java module system we need an explicit provides clause in module-info like:\n\n" +
        " provides io.avaje.inject.spi.BeanContextFactory with org.example._DI$BeanContextFactory;\n\n" +
        " Otherwise perhaps using Gradle and IDEA but with a setup issue?" +
        " Review IntelliJ Settings / Build / Build tools / Gradle - 'Build and run using' value and set that to 'Gradle'. " +
        " Refer to https://avaje.io/inject#gradle");
    }
    log.debug("building context with modules {}", moduleNames);
    Builder builder = Builder.newBuilder(suppliedBeans, enrichBeans);
    for (BeanContextFactory factory : factoryOrder.factories()) {
      factory.build(builder);
    }
    return builder.build(shutdownHook);
  }

  /**
   * Return the type that we map the supplied bean to.
   */
  private Class<?> suppliedType(Class<?> suppliedClass) {
    Class<?> suppliedSuper = suppliedClass.getSuperclass();
    if (Object.class.equals(suppliedSuper)) {
      return suppliedClass;
    } else {
      // prefer to use the super type of the supplied bean (test double)
      return suppliedSuper;
    }
  }

  /**
   * Helper to order the BeanContextFactory based on dependsOn.
   */
  static class FactoryOrder {

    private final Set<String> includeModules;
    private final boolean suppliedBeans;
    private final boolean ignoreMissingModuleDependencies;

    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<BeanContextFactory> factories = new ArrayList<>();
    private final List<FactoryState> queue = new ArrayList<>();
    private final List<FactoryState> queueNoDependencies = new ArrayList<>();

    private final Map<String, FactoryList> providesMap = new HashMap<>();

    FactoryOrder(Set<String> includeModules, boolean suppliedBeans, boolean ignoreMissingModuleDependencies) {
      this.includeModules = includeModules;
      this.suppliedBeans = suppliedBeans;
      this.ignoreMissingModuleDependencies = ignoreMissingModuleDependencies;
    }

    void add(BeanContextFactory factory) {

      if (includeModule(factory)) {
        FactoryState wrappedFactory = new FactoryState(factory);
        providesMap.computeIfAbsent(factory.getName(), s -> new FactoryList()).add(wrappedFactory);
        if (!isEmpty(factory.getProvides())) {
          for (String feature : factory.getProvides()) {
            providesMap.computeIfAbsent(feature, s -> new FactoryList()).add(wrappedFactory);
          }
        }
        if (isEmpty(factory.getDependsOn())) {
          if (!isEmpty(factory.getProvides())) {
            // only has 'provides' so we can push this
            push(wrappedFactory);
          } else {
            // hold until after all the 'provides only' modules are added
            queueNoDependencies.add(wrappedFactory);
          }
        } else {
          // queue it to process by dependency ordering
          queue.add(wrappedFactory);
        }
      }
    }

    private boolean isEmpty(String[] values) {
      return values == null || values.length == 0;
    }

    /**
     * Return true of the factory (for the module) should be included.
     */
    private boolean includeModule(BeanContextFactory factory) {
      return includeModules.isEmpty() || includeModules.contains(factory.getName());
    }

    /**
     * Push the factory onto the build order (the wiring order for modules).
     */
    private void push(FactoryState factory) {
      factory.setPushed();
      factories.add(factory.getFactory());
      moduleNames.add(factory.getName());
    }

    /**
     * Order the factories returning the ordered list of module names.
     */
    Set<String> orderFactories() {
      // push the 'no dependency' modules after the 'provides only' ones
      // as this is more intuitive for the simple (only provides modules case)
      for (FactoryState factoryState : queueNoDependencies) {
        push(factoryState);
      }
      processQueue();
      return moduleNames;
    }

    /**
     * Return the list of factories in the order they should be built.
     */
    List<BeanContextFactory> factories() {
      return factories;
    }

    /**
     * Process the queue pushing the factories in order to satisfy dependencies.
     */
    private void processQueue() {

      int count;
      do {
        count = processQueuedFactories();
      } while (count > 0);

      if (suppliedBeans || ignoreMissingModuleDependencies) {
        // just push everything left assuming supplied beans
        // will satisfy the required dependencies
        for (FactoryState factoryState : queue) {
          push(factoryState);
        }

      } else if (!queue.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (FactoryState factory : queue) {
          sb.append("Module [").append(factory.getName()).append("] has unsatisfied dependencies on modules:");
          for (String depModuleName : factory.getDependsOn()) {
            if (!moduleNames.contains(depModuleName)) {
              sb.append(String.format(" [%s]", depModuleName));
            }
          }
        }

        sb.append(". Modules that were loaded ok are:").append(moduleNames);
        sb.append(". Consider using BeanContextBuilder.withIgnoreMissingModuleDependencies() or BeanContextBuilder.withSuppliedBeans(...)");
        throw new IllegalStateException(sb.toString());
      }
    }

    /**
     * Process the queued factories pushing them when all their (module) dependencies
     * are satisfied.
     * <p>
     * This returns the number of factories added so once this returns 0 it is done.
     */
    private int processQueuedFactories() {

      int count = 0;
      Iterator<FactoryState> it = queue.iterator();
      while (it.hasNext()) {
        FactoryState factory = it.next();
        if (satisfiedDependencies(factory)) {
          // push the factory onto the build order
          it.remove();
          push(factory);
          count++;
        }
      }
      return count;
    }

    /**
     * Return true if the (module) dependencies are satisfied for this factory.
     */
    private boolean satisfiedDependencies(FactoryState factory) {
      for (String moduleOrFeature : factory.getDependsOn()) {
        FactoryList factories = providesMap.get(moduleOrFeature);
        if (factories == null || !factories.allPushed()) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Wrapper on Factory holding the pushed state.
   */
  private static class FactoryState {

    private final BeanContextFactory factory;
    private boolean pushed;

    private FactoryState(BeanContextFactory factory) {
      this.factory = factory;
    }

    /**
     * Set when factory is pushed onto the build/wiring order.
     */
    void setPushed() {
      this.pushed = true;
    }

    boolean isPushed() {
      return pushed;
    }

    BeanContextFactory getFactory() {
      return factory;
    }

    String getName() {
      return factory.getName();
    }

    String[] getDependsOn() {
      return factory.getDependsOn();
    }
  }

  /**
   * List of factories for a given name or feature.
   */
  private static class FactoryList {

    private final List<FactoryState> factories = new ArrayList<>();

    void add(FactoryState factory) {
      factories.add(factory);
    }

    /**
     * Return true if all factories here have been pushed onto the build order.
     */
    boolean allPushed() {
      for (FactoryState factory : factories) {
        if (!factory.isPushed()) {
          return false;
        }
      }
      return true;
    }
  }

}
