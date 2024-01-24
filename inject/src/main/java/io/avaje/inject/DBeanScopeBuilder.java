package io.avaje.inject;

import io.avaje.applog.AppLog;
import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.*;
import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;
import jakarta.inject.Provider;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Collections.emptySet;

/** Build a bean scope with options for shutdown hook and supplying test doubles. */
@NonNullApi
final class DBeanScopeBuilder implements BeanScopeBuilder.ForTesting {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");

  private final List<SuppliedBean> suppliedBeans = new ArrayList<>();
  @SuppressWarnings("rawtypes")
  private final List<EnrichBean> enrichBeans = new ArrayList<>();
  private final Set<Module> includeModules = new LinkedHashSet<>();
  private final List<Runnable> postConstructList = new ArrayList<>();
  private final List<Consumer<BeanScope>> postConstructConsumerList = new ArrayList<>();
  private final List<ClosePair> preDestroyList = new ArrayList<>();
  private BeanScope parent;
  private boolean parentOverride = true;
  private boolean shutdownHook;
  private ClassLoader classLoader;
  private PropertyRequiresPlugin propertyRequiresPlugin;
  private Set<String> profiles;

  /** Create a BeanScopeBuilder to ultimately load and return a new BeanScope. */
  DBeanScopeBuilder() {}

  @Override
  public ForTesting forTesting() {
    return this;
  }

  @Override
  public BeanScopeBuilder shutdownHook(boolean shutdownHook) {
    this.shutdownHook = shutdownHook;
    return this;
  }

  @Override
  public BeanScopeBuilder modules(Module... modules) {
    this.includeModules.addAll(Arrays.asList(modules));
    return this;
  }

  @Override
  public void propertyPlugin(PropertyRequiresPlugin propertyRequiresPlugin) {
    this.propertyRequiresPlugin = propertyRequiresPlugin;
  }

  @Override
  public PropertyRequiresPlugin propertyPlugin() {
    if (propertyRequiresPlugin == null) {
      initClassLoader();
      initPropertyPlugin();
    }
    return propertyRequiresPlugin;
  }

  @Override
  public BeanScopeBuilder beans(Object... beans) {
    for (final Object bean : beans) {
      suppliedBeans.add(SuppliedBean.of(superOf(bean.getClass()), bean));
    }
    return this;
  }

  @Override
  public <D> BeanScopeBuilder bean(Class<D> type, D bean) {
    return bean(null, type, bean);
  }

  @Override
  public <D> BeanScopeBuilder bean(@Nullable String name, Class<D> type, D bean) {
    suppliedBeans.add(SuppliedBean.of(name, type, bean));
    return this;
  }

  @Override
  public <D> BeanScopeBuilder bean(Type type, D bean) {
    return bean(null, type, bean);
  }

  @Override
  public <D> BeanScopeBuilder bean(@Nullable String name, Type type, D bean) {
    suppliedBeans.add(SuppliedBean.ofType(name, type, bean));
    return this;
  }

  @Override
  public BeanScopeBuilder profiles(String... profiles) {
    this.profiles = Set.of(profiles);
    return this;
  }

  @Override
  public <D> BeanScopeBuilder provideDefault(@Nullable String name, Type type, Supplier<D> supplier) {
    final Provider<D> provider = supplier::get;
    suppliedBeans.add(SuppliedBean.secondary(name, type, provider));
    return this;
  }

  @Override
  public BeanScopeBuilder addPostConstruct(Runnable postConstructHook) {
    postConstructList.add(postConstructHook);
    return this;
  }

  @Override
  public BeanScopeBuilder addPostConstruct(Consumer<BeanScope> postConstructConsumer) {
    this.postConstructConsumerList.add(postConstructConsumer);
    return this;
  }

  @Override
  public BeanScopeBuilder addPreDestroy(AutoCloseable preDestroyHook) {
    return addPreDestroy(preDestroyHook, 1000);
  }

  @Override
  public BeanScopeBuilder addPreDestroy(AutoCloseable preDestroyHook, int priority) {
    preDestroyList.add(new ClosePair(priority, preDestroyHook));
    return this;
  }

  @Override
  public BeanScopeBuilder classLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public BeanScopeBuilder parent(BeanScope parent) {
    this.parent = parent;
    return this;
  }

  @Override
  public BeanScopeBuilder parent(BeanScope parent, boolean parentOverride) {
    this.parent = parent;
    this.parentOverride = parentOverride;
    return this;
  }

  @Override
  public BeanScopeBuilder.ForTesting mock(Type type) {
    return mock(type, null);
  }

  @Override
  public BeanScopeBuilder.ForTesting mock(Type type, String name) {
    suppliedBeans.add(SuppliedBean.ofType(name, type, null));
    return this;
  }

  @Override
  public <D> BeanScopeBuilder.ForTesting mock(Class<D> type, Consumer<D> consumer) {
    return mock(type, null, consumer);
  }

  private <D> BeanScopeBuilder.ForTesting mock(Class<D> type, @Nullable String name, @Nullable Consumer<D> consumer) {
    suppliedBeans.add(SuppliedBean.of(name, type, consumer));
    return this;
  }

  @Override
  public BeanScopeBuilder.ForTesting spy(Type type) {
    return spy(type, null, null);
  }

  @Override
  public BeanScopeBuilder.ForTesting spy(Type type, String name) {
    return spy(type, name, null);
  }

  @Override
  public <D> BeanScopeBuilder.ForTesting spy(Class<D> type, Consumer<D> consumer) {
    return spy(type, null, consumer);
  }

  private <D> BeanScopeBuilder.ForTesting spy(Class<D> type, @Nullable String name, @Nullable Consumer<D> consumer) {
    enrichBeans.add(new EnrichBean<>(type, name, consumer));
    return this;
  }

  private <D> BeanScopeBuilder.ForTesting spy(Type type, @Nullable String name, @Nullable Consumer<D> consumer) {
    enrichBeans.add(new EnrichBean<>(type, name, consumer));
    return this;
  }

  private void initClassLoader() {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
  }

  private void initPropertyPlugin() {
    propertyRequiresPlugin =
      ServiceLoader.load(PropertyRequiresPlugin.class, classLoader)
        .findFirst()
        .orElse(defaultPropertyPlugin());
  }

  private PropertyRequiresPlugin defaultPropertyPlugin() {
    return detectAvajeConfig() ? new DConfigProps() : new DSystemProps();
  }

  private boolean detectAvajeConfig() {
    if (ModuleLayer.boot().findModule("io.avaje.config").isPresent()) {
      return true;
    }
    try {
      Class.forName("io.avaje.config.Configuration", false, classLoader);
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  private void initProfiles() {
    if (profiles == null) {
      profiles =
          propertyRequiresPlugin
              .get("avaje.profiles")
              .map(p -> Set.of(p.split(",")))
              .orElse(emptySet());
    }
  }

  @Override
  public BeanScope build() {
    final var start = System.currentTimeMillis();
    // load and apply plugins first
    initClassLoader();
    if (propertyRequiresPlugin == null) {
      initPropertyPlugin();
    }

    ServiceLoader.load(Plugin.class, classLoader).forEach(plugin -> plugin.apply(this));
    // sort factories by dependsOn
    final FactoryOrder factoryOrder = new FactoryOrder(parent, includeModules, !suppliedBeans.isEmpty());
    if (factoryOrder.isEmpty()) {
      ServiceLoader.load(Module.class, classLoader).forEach(factoryOrder::add);
    }

    final Set<String> moduleNames = factoryOrder.orderFactories();
    if (moduleNames.isEmpty()) {
      throw new IllegalStateException(
          "Could not find any avaje modules."
              + " Perhaps using Gradle and IDEA but with a setup issue?"
              + " Review IntelliJ Settings / Build / Build tools / Gradle - 'Build and run using' value and set that to 'Gradle'. "
              + " Refer to https://avaje.io/inject#gradle");
    }

    final var level = propertyRequiresPlugin.contains("printModules") ? INFO : DEBUG;
    initProfiles();
    log.log(level, "building with avaje modules {0} profiles {1}", moduleNames, profiles);

    final Builder builder = Builder.newBuilder(profiles, propertyRequiresPlugin, suppliedBeans, enrichBeans, parent, parentOverride);
    for (final Module factory : factoryOrder.factories()) {
      factory.build(builder);
    }

    postConstructList.forEach(builder::addPostConstruct);
    postConstructConsumerList.forEach(builder::addPostConstruct);
    for (ClosePair closePair : preDestroyList) {
      builder.addPreDestroy(closePair.closeable(), closePair.priority());
    }
    return builder.build(shutdownHook, start);
  }

  /** Return the type that we map the supplied bean to. */
  private static Class<?> superOf(Class<?> suppliedClass) {
    final Class<?> suppliedSuper = suppliedClass.getSuperclass();
    if (Object.class.equals(suppliedSuper)) {
      return suppliedClass;
    } else {
      // prefer to use the super type of the supplied bean (test double)
      return suppliedSuper;
    }
  }

  /** Helper to order the BeanContextFactory based on dependsOn. */
  static class FactoryOrder {

    private final BeanScope parent;
    private final boolean suppliedBeans;
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<Module> factories = new ArrayList<>();
    private final List<FactoryState> queue = new ArrayList<>();
    private final List<FactoryState> queueNoDependencies = new ArrayList<>();

    private final Map<String, FactoryList> providesMap = new HashMap<>();

    FactoryOrder(BeanScope parent, Set<Module> includeModules, boolean suppliedBeans) {
      this.parent = parent;
      this.factories.addAll(includeModules);
      this.suppliedBeans = suppliedBeans;
      for (final Module includeModule : includeModules) {
        moduleNames.add(includeModule.getClass().getName());
      }
    }

    void add(Module module) {
      final FactoryState factoryState = new FactoryState(module);
      providesMap.computeIfAbsent(module.getClass().getTypeName(), s -> new FactoryList()).add(factoryState);
      addFactoryProvides(factoryState, module.provides());
      addFactoryProvides(factoryState, module.autoProvides());
      addFactoryProvides(factoryState, module.autoProvidesAspects());

      if (factoryState.isRequiresEmpty()) {
        if (factoryState.explicitlyProvides()) {
          // push immediately when explicitly 'provides' with no 'requires'
          push(factoryState);
        } else {
          // hold until after all the explicitly 'provides' modules are added
          queueNoDependencies.add(factoryState);
        }
      } else {
        // queue it to process by dependency ordering
        queue.add(factoryState);
      }
    }

    private void addFactoryProvides(FactoryState factoryState, Class<?>[] provides) {
      for (final Class<?> feature : provides) {
        providesMap.computeIfAbsent(feature.getTypeName(), s -> new FactoryList()).add(factoryState);
      }
    }

    /** Push the factory onto the build order (the wiring order for modules). */
    private void push(FactoryState factory) {
      factory.setPushed();
      factories.add(factory.factory());
      moduleNames.add(factory.factory().getClass().getName());
    }

    /** Order the factories returning the ordered list of module names. */
    Set<String> orderFactories() {
      // push the 'no dependency' modules after the 'provides only' ones
      // as this is more intuitive for the simple (only provides modules case)
      for (final FactoryState factoryState : queueNoDependencies) {
        push(factoryState);
      }
      processQueue();
      return moduleNames;
    }

    /** Return the list of factories in the order they should be built. */
    List<Module> factories() {
      return factories;
    }

    /** Process the queue pushing the factories in order to satisfy dependencies. */
    private void processQueue() {
      int count;
      do {
        count = processQueuedFactories();
      } while (count > 0);

      if (suppliedBeans) {
        // just push everything left assuming supplied beans
        // will satisfy the required dependencies
        for (final FactoryState factoryState : queue) {
          push(factoryState);
        }
      } else if (!queue.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
        for (final FactoryState factory : queue) {
          sb.append("Module [").append(factory).append("] has unsatisfied");
          unsatisfiedRequires(sb, factory.requires(), "requires");
          unsatisfiedRequires(sb, factory.requiresPackages(), "requiresPackages");
          unsatisfiedRequires(sb, factory.autoRequires(), "autoRequires");
        }
        sb.append(" - none of the loaded modules ").append(moduleNames).append(" have this in their @InjectModule( provides = ... ). ");
        if (parent != null) {
          sb.append("The parent BeanScope ").append(parent).append(" also does not provide this dependency. ");
        }
        sb.append("Either @InjectModule requires/provides are not aligned? or add external dependencies via BeanScopeBuilder.bean()?");
        throw new IllegalStateException(sb.toString());
      }
    }

    private void unsatisfiedRequires(StringBuilder sb, Class<?>[] requiredType, String requires) {
      for (final Class<?> depModuleName : requiredType) {
        if (notProvided(depModuleName.getTypeName())) {
          sb.append(String.format(" %s [%s]", requires, depModuleName.getTypeName()));
        }
      }
    }

    private boolean notProvided(String dependency) {
      if (parent != null && parent.contains(dependency)) {
        return false;
      }
      final FactoryList factoryList = providesMap.get(dependency);
      return (factoryList == null || !factoryList.allPushed());
    }

    /**
     * Process the queued factories pushing them when all their (module) dependencies are satisfied.
     *
     * <p>This returns the number of factories added so once this returns 0 it is done.
     */
    private int processQueuedFactories() {
      int count = 0;
      final Iterator<FactoryState> it = queue.iterator();
      while (it.hasNext()) {
        final FactoryState factory = it.next();
        if (satisfiedDependencies(factory)) {
          // push the factory onto the build order
          it.remove();
          push(factory);
          count++;
        }
      }
      return count;
    }

    /** Return true if the (module) requires dependencies are satisfied for this factory. */
    private boolean satisfiedDependencies(FactoryState factory) {
      return satisfiedDependencies(factory.requires())
        && satisfiedDependencies(factory.requiresPackages())
        && satisfiedDependencies(factory.autoRequiresAspects())
        && satisfiedDependencies(factory.autoRequires());
    }

    private boolean satisfiedDependencies(Class<?>[] requires) {
      for (final Class<?> dependency : requires) {
        if (notProvided(dependency.getTypeName())) {
          return false;
        }
      }
      return true;
    }

    boolean isEmpty() {
      return factories.isEmpty();
    }
  }

  /** Wrapper on Factory holding the pushed state. */
  private static class FactoryState {

    private final Module factory;
    private boolean pushed;

    private FactoryState(Module factory) {
      this.factory = factory;
    }

    /** Set when factory is pushed onto the build/wiring order. */
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

    Class<?>[] requiresPackages() {
      return factory.requiresPackages();
    }

    Class<?>[] autoRequires() {
      return factory.autoRequires();
    }

    Class<?>[] autoRequiresAspects() {
      return factory.autoRequiresAspects();
    }

    @Override
    public String toString() {
      return factory.getClass().getTypeName();
    }

    boolean isRequiresEmpty() {
      return isEmpty(factory.requires()) && isEmpty(factory.requiresPackages())
        && isEmpty(factory.autoRequires()) && isEmpty(factory.autoRequiresAspects());
    }

    boolean explicitlyProvides() {
      return !isEmpty(factory.provides());
    }

    private boolean isEmpty(@Nullable Class<?>[] values) {
      return values == null || values.length == 0;
    }
  }

  /** List of factories for a given name or feature. */
  private static class FactoryList {

    private final List<FactoryState> factories = new ArrayList<>();

    void add(FactoryState factory) {
      factories.add(factory);
    }

    /** Return true if all factories here have been pushed onto the build order. */
    boolean allPushed() {
      for (final FactoryState factory : factories) {
        if (!factory.isPushed()) {
          return false;
        }
      }
      return true;
    }
  }
}
