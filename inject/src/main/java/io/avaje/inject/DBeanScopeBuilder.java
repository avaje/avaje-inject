package io.avaje.inject;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.EnrichBean;
import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.SuppliedBean;
import io.avaje.lang.Nullable;

import java.lang.System.Logger.Level;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

/**
 * Build a bean scope with options for shutdown hook and supplying test doubles.
 */
class DBeanScopeBuilder implements BeanScopeBuilder.ForTesting {

  private static final System.Logger log = System.getLogger("io.avaje.inject");

  @SuppressWarnings("rawtypes")
  private final List<SuppliedBean> suppliedBeans = new ArrayList<>();

  @SuppressWarnings("rawtypes")
  private final List<EnrichBean> enrichBeans = new ArrayList<>();

  private final Set<Module> includeModules = new LinkedHashSet<>();

  private BeanScope parent;
  private boolean parentOverride;
  private boolean shutdownHook;

  /**
   * Create a BeanScopeBuilder to ultimately load and return a new BeanScope.
   */
  DBeanScopeBuilder() {
  }

  @Override
  public ForTesting forTesting() {
    return this;
  }

  @Override
  public BeanScopeBuilder withShutdownHook(boolean shutdownHook) {
    this.shutdownHook = shutdownHook;
    return this;
  }

  @Override
  public BeanScopeBuilder withModules(Module... modules) {
    this.includeModules.addAll(Arrays.asList(modules));
    return this;
  }

  @Override
  public BeanScopeBuilder withBeans(Object... beans) {
    for (Object bean : beans) {
      suppliedBeans.add(SuppliedBean.of(superOf(bean.getClass()), bean));
    }
    return this;
  }

  @Override
  public <D> BeanScopeBuilder withBean(Class<D> type, D bean) {
    return withBean(null, type, bean);
  }

  @Override
  public <D> BeanScopeBuilder withBean(@Nullable String name, Class<D> type, D bean) {
    suppliedBeans.add(SuppliedBean.of(name, type, bean));
    return this;
  }

  @Override
  public <D> BeanScopeBuilder withBean(Type type, D bean) {
    return withBean(null, type, bean);
  }

  @Override
  public <D> BeanScopeBuilder withBean(@Nullable String name, Type type, D bean) {
    suppliedBeans.add(SuppliedBean.ofType(name, type, bean));
    return this;
  }

  @Override
  public BeanScopeBuilder withParent(BeanScope parent) {
    return withParent(parent, true);
  }

  @Override
  public BeanScopeBuilder withParent(BeanScope parent, boolean parentOverride) {
    this.parent = parent;
    this.parentOverride = parentOverride;
    return this;
  }

  @Override
  public BeanScopeBuilder.ForTesting withMock(Class<?> type) {
    return withMock(type, null, null);
  }

  public BeanScopeBuilder.ForTesting withMock(Class<?> type, String name) {
    return withMock(type, name, null);
  }

  @Override
  public <D> BeanScopeBuilder.ForTesting withMock(Class<D> type, Consumer<D> consumer) {
    return withMock(type, null, consumer);
  }

  private <D> BeanScopeBuilder.ForTesting withMock(Class<D> type, @Nullable String name, @Nullable Consumer<D> consumer) {
    suppliedBeans.add(SuppliedBean.of(name, type, null, consumer));
    return this;
  }

  @Override
  public BeanScopeBuilder.ForTesting withSpy(Class<?> type) {
    return spy(type, null, null);
  }

  public BeanScopeBuilder.ForTesting withSpy(Class<?> type, String name) {
    return spy(type, name, null);
  }

  @Override
  public <D> BeanScopeBuilder.ForTesting withSpy(Class<D> type, Consumer<D> consumer) {
    return spy(type, null, consumer);
  }

  private <D> BeanScopeBuilder.ForTesting spy(Class<D> type, @Nullable String name, @Nullable Consumer<D> consumer) {
    enrichBeans.add(new EnrichBean<>(type, name, consumer));
    return this;
  }

  @Override
  public BeanScope build() {
    // sort factories by dependsOn
    FactoryOrder factoryOrder = new FactoryOrder(includeModules, !suppliedBeans.isEmpty());
    if (factoryOrder.isEmpty()) {
      ServiceLoader.load(Module.class).forEach(factoryOrder::add);
    }

    Set<String> moduleNames = factoryOrder.orderFactories();
    if (moduleNames.isEmpty()) {
      throw new IllegalStateException("No modules found. When using java module system we need an explicit provides clause in module-info like:\n\n" +
        " provides io.avaje.inject.spi.Module with org.example.ExampleModule;\n\n" +
        " Otherwise perhaps using Gradle and IDEA but with a setup issue?" +
        " Review IntelliJ Settings / Build / Build tools / Gradle - 'Build and run using' value and set that to 'Gradle'. " +
        " Refer to https://avaje.io/inject#gradle");
    }
    log.log(Level.DEBUG, "building with modules %s", moduleNames);
    Builder builder = Builder.newBuilder(suppliedBeans, enrichBeans, parent, parentOverride);
    for (Module factory : factoryOrder.factories()) {
      factory.build(builder);
    }
    return builder.build(shutdownHook);
  }

  /**
   * Return the type that we map the supplied bean to.
   */
  private static Class<?> superOf(Class<?> suppliedClass) {
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

    private final boolean suppliedBeans;
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<Module> factories = new ArrayList<>();
    private final List<FactoryState> queue = new ArrayList<>();
    private final List<FactoryState> queueNoDependencies = new ArrayList<>();

    private final Map<String, FactoryList> providesMap = new HashMap<>();

    FactoryOrder(Set<Module> includeModules, boolean suppliedBeans) {
      this.factories.addAll(includeModules);
      this.suppliedBeans = suppliedBeans;
      for (Module includeModule : includeModules) {
        moduleNames.add(includeModule.getClass().getName());
      }
    }

    void add(Module factory) {
      FactoryState wrappedFactory = new FactoryState(factory);
      providesMap.computeIfAbsent(factory.getClass().getTypeName(), s -> new FactoryList()).add(wrappedFactory);
      if (!isEmpty(factory.provides())) {
        for (Class<?> feature : factory.provides()) {
          providesMap.computeIfAbsent(feature.getTypeName(), s -> new FactoryList()).add(wrappedFactory);
        }
      }
      if (isEmpty(factory.requires())) {
        if (!isEmpty(factory.provides())) {
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

    private boolean isEmpty(@Nullable Class<?>[] values) {
      return values == null || values.length == 0;
    }

    /**
     * Push the factory onto the build order (the wiring order for modules).
     */
    private void push(FactoryState factory) {
      factory.setPushed();
      factories.add(factory.factory());
      moduleNames.add(factory.factory().getClass().getName());
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
    List<Module> factories() {
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

      if (suppliedBeans) {
        // just push everything left assuming supplied beans
        // will satisfy the required dependencies
        for (FactoryState factoryState : queue) {
          push(factoryState);
        }

      } else if (!queue.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (FactoryState factory : queue) {
          sb.append("Module [").append(factory.getClass()).append("] has unsatisfied dependencies on modules:");
          for (Class<?> depModuleName : factory.requires()) {
            if (!moduleNames.contains(depModuleName.getName())) {
              sb.append(String.format(" [%s]", depModuleName));
            }
          }
        }

        sb.append(". Modules that were loaded ok are:").append(moduleNames);
        sb.append(". Maybe need to add external dependencies via BeanScopeBuilder.withBean()?");
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
      for (Class<?> moduleOrFeature : factory.requires()) {
        FactoryList factories = providesMap.get(moduleOrFeature.getTypeName());
        if (factories == null || !factories.allPushed()) {
          return false;
        }
      }
      return true;
    }

    boolean isEmpty() {
      return factories.isEmpty();
    }
  }

  /**
   * Wrapper on Factory holding the pushed state.
   */
  private static class FactoryState {

    private final Module factory;
    private boolean pushed;

    private FactoryState(Module factory) {
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

    Module factory() {
      return factory;
    }

    Class<?>[] requires() {
      return factory.requires();
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
