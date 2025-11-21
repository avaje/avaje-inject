package io.avaje.inject;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Collections.emptySet;

import java.lang.System.Logger.Level;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

import io.avaje.applog.AppLog;
import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.ClosePair;
import io.avaje.inject.spi.ConfigPropertyPlugin;
import io.avaje.inject.spi.EnrichBean;
import io.avaje.inject.spi.ModuleOrdering;
import io.avaje.inject.spi.SuppliedBean;

/** Build a bean scope with options for shutdown hook and supplying test doubles. */
final class DBeanScopeBuilder implements BeanScopeBuilder.ForTesting {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");

  private final List<SuppliedBean> suppliedBeans = new ArrayList<>();
  @SuppressWarnings("rawtypes")
  private final List<EnrichBean> enrichBeans = new ArrayList<>();
  private final Set<AvajeModule> includeModules = new LinkedHashSet<>();
  private final List<Runnable> postConstructList = new ArrayList<>();
  private final List<Consumer<BeanScope>> postConstructConsumerList = new ArrayList<>();
  private final List<ClosePair> preDestroyList = new ArrayList<>();
  private @Nullable BeanScope parent;
  private boolean parentOverride = true;
  private boolean shutdownHook;
  private @Nullable ClassLoader classLoader;
  private @Nullable ConfigPropertyPlugin propertyPlugin;
  private @Nullable Set<String> profiles;

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
  public BeanScopeBuilder modules(AvajeModule... modules) {
    this.includeModules.addAll(Arrays.asList(modules));
    return this;
  }

  @Override
  public void configPlugin(ConfigPropertyPlugin propertyPlugin) {
    this.propertyPlugin = propertyPlugin;
  }

  @Override
  public ConfigPropertyPlugin configPlugin() {
    if (propertyPlugin == null) {
      propertyPlugin = defaultPropertyPlugin();
    }
    return propertyPlugin;
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
  public BeanScopeBuilder.ForTesting mock(Type type,  @Nullable String name) {
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
  private ConfigPropertyPlugin defaultPropertyPlugin() {
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
        propertyPlugin
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

    var serviceLoader = new DServiceLoader(classLoader);
    if (propertyPlugin == null) {
      propertyPlugin = serviceLoader.propertyPlugin().orElseGet(this::defaultPropertyPlugin);
    }

    serviceLoader.plugins().forEach(plugin -> plugin.apply(this));

    // sort factories by dependsOn
    ModuleOrdering factoryOrder = new FactoryOrder(parent, includeModules, !suppliedBeans.isEmpty());

    if (includeModules.isEmpty()) {
      var modules = serviceLoader.modules();
      // prefer generated ModuleOrdering if provided
      factoryOrder = serviceLoader.moduleOrdering()
        .filter(o -> o.supportsExpected(modules))
        .orElse(factoryOrder);
      modules.forEach(factoryOrder::add);
    }

    final var moduleNames = factoryOrder.orderModules();

    final var level = propertyPlugin.contains("printModules") ? INFO : DEBUG;
    initProfiles();
    log.log(level, "building with avaje modules {0} profiles {1}", moduleNames, profiles);

    final var builder = Builder.newBuilder(profiles, propertyPlugin, suppliedBeans, enrichBeans, parent, parentOverride);
    for (final var factory : factoryOrder.factories()) {
      builder.currentModule(factory.getClass());
      builder.currentScopes(factory.definesScopes());
      factory.build(builder);
    }

    if (moduleNames.isEmpty()) {
      log.log(
          Level.ERROR,
          "Could not find any AvajeModule instances to wire. Possible Causes: \n"
              + "1. No beans have been defined.\n"
              + "2. The avaje-inject-generator depedency was not available during compilation"
              + (Runtime.version().feature() >= 23
                  ? " or -proc full was not enabled in maven compiler"
                  : "")
              + "\n3. Perhaps using Gradle and a misconfigured IDE? Refer to https://avaje.io/inject#gradle");
    }

    postConstructList.forEach(builder::addPostConstruct);
    postConstructConsumerList.forEach(builder::addPostConstruct);
    for (var closePair : preDestroyList) {
      builder.addPreDestroy(closePair.closeable(), closePair.priority());
    }
    return builder.build(shutdownHook, start);
  }

  /** Return the type that we map the supplied bean to. */
  private static Class<?> superOf(Class<?> suppliedClass) {
    final Class<?> suppliedSuper = suppliedClass.getSuperclass();
    if (Object.class.equals(suppliedSuper)) {
      return suppliedClass;
    }
  // prefer to use the super type of the supplied bean (test double)
    return suppliedSuper;
  }

  /** Helper to order the BeanContextFactory based on dependsOn. */
  static class FactoryOrder implements ModuleOrdering {

    private final @Nullable BeanScope parent;
    private final boolean suppliedBeans;
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<AvajeModule> factories = new ArrayList<>();
    private final List<FactoryState> queue = new ArrayList<>();
    private final List<FactoryState> queueNoDependencies = new ArrayList<>();
    private final Map<String, FactoryList> providesMap = new HashMap<>();

    FactoryOrder(@Nullable BeanScope parent, Set<AvajeModule> includeModules, boolean suppliedBeans) {
      this.parent = parent;
      this.suppliedBeans = suppliedBeans;
      for (final AvajeModule includeModule : includeModules) {
        add(includeModule);
      }
    }

    @Override
    public void add(AvajeModule module) {
      final var factoryState = new FactoryState(module);
      providesMap
        .computeIfAbsent(module.getClass().getTypeName(), s -> new FactoryList())
        .add(factoryState);

      addFactoryProvides(factoryState, module.providesBeans());

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

    private void addFactoryProvides(FactoryState factoryState, String[] provides) {
      for (final var feature : provides) {
        providesMap.computeIfAbsent(feature, s -> new FactoryList()).add(factoryState);
      }
    }

    /** Push the factory onto the build order (the wiring order for modules). */
    private void push(FactoryState factory) {
      factory.setPushed();
      factories.add(factory.factory());
      moduleNames.add(factory.factory().getClass().getName());
    }

    @Override
    public Set<String> orderModules() {
      // push the 'no dependency' modules after the 'provides only' ones
      // as this is more intuitive for the simple (only provides modules case)
      for (final FactoryState factoryState : queueNoDependencies) {
        push(factoryState);
      }
      processQueue();
      return moduleNames;
    }

    @Override
    public List<AvajeModule> factories() {
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
        final var sb = new StringBuilder();
        for (final FactoryState factory : queue) {
          sb.append("Module [").append(factory).append("] has unsatisfied");
          unsatisfiedRequires(sb, factory.requires(), "requires");
          unsatisfiedRequires(sb, factory.requiresPackages(), "requiresPackages");
        }
        sb.append(" - none of the loaded modules ").append(moduleNames).append(" have this in their @InjectModule( provides = ... ). ");
        if (parent != null) {
          sb.append("The parent BeanScope ").append(parent).append(" also does not provide this dependency. ");
        }
        sb.append("Either @InjectModule requires/provides are not aligned? or add external dependencies via BeanScopeBuilder.bean()?");
        throw new IllegalStateException(sb.toString());
      }
    }

    private void unsatisfiedRequires(StringBuilder sb, String[] requiredType, String requires) {
      for (final var depModuleName : requiredType) {
        if (notProvided(depModuleName)) {
          sb.append(String.format(" %s [%s]", requires, depModuleName));
        }
      }
    }

    private boolean notProvided(String dependency) {
      if (parent != null && (parent.contains(dependency) || parent.customScopeAnnotations().contains(dependency))) {
        return false;
      }
      final var factoryList = providesMap.get(dependency);
      return factoryList == null || !factoryList.allPushed();
    }

    /**
     * Process the queued factories pushing them when all their (module) dependencies are satisfied.
     *
     * <p>This returns the number of factories added so once this returns 0 it is done.
     */
    private int processQueuedFactories() {
      int count = 0;
      final var it = queue.iterator();
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
          && satisfiedDependencies(factory.requiresPackages());
    }

    private boolean satisfiedDependencies(String[] requires) {
      for (final var dependency : requires) {
        if (notProvided(dependency)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean isEmpty() {
      return factories.isEmpty();
    }
  }

  /** Wrapper on Factory holding the pushed state. */
  private static class FactoryState {

    private final AvajeModule factory;
    private boolean pushed;

    private FactoryState(AvajeModule factory) {
      this.factory = factory;
    }

    /** Set when factory is pushed onto the build/wiring order. */
    void setPushed() {
      this.pushed = true;
    }

    boolean isPushed() {
      return pushed;
    }

    AvajeModule factory() {
      return factory;
    }

    String[] requires() {
      return factory.requiresBeans();
    }

    String[] requiresPackages() {
      return factory.requiresPackagesFromType();
    }

    @Override
    public String toString() {
      return factory.getClass().getTypeName();
    }

    boolean isRequiresEmpty() {
      return isEmpty(factory.requiresBeans()) && isEmpty(factory.requiresPackagesFromType());
    }

    boolean explicitlyProvides() {
      return !isEmpty(factory.providesBeans());
    }

    private boolean isEmpty(@Nullable String[] values) {
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
