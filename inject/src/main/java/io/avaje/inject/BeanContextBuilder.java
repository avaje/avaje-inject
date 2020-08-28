package io.avaje.inject;

import io.avaje.inject.core.BeanContextFactory;
import io.avaje.inject.core.Builder;
import io.avaje.inject.core.BuilderFactory;
import io.avaje.inject.core.EnrichBean;
import io.avaje.inject.core.SuppliedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Build a bean context with options for shutdown hook and supplying test doubles.
 * <p>
 * We would choose to use BeanContextBuilder in test code (for component testing) as it gives us
 * the ability to inject test doubles, mocks, spy's etc.
 * </p>
 *
 * <pre>{@code
 *
 *   @Test
 *   public void someComponentTest() {
 *
 *     MyRedisApi mockRedis = mock(MyRedisApi.class);
 *     MyDbApi mockDatabase = mock(MyDbApi.class);
 *
 *     try (BeanContext context = new BeanContextBuilder()
 *       .withBeans(mockRedis, mockDatabase)
 *       .build()) {
 *
 *       // built with test doubles injected ...
 *       CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
 *       coffeeMaker.makeIt();
 *
 *       assertThat(...
 *     }
 *   }
 *
 * }</pre>
 */
public class BeanContextBuilder {

  private static final Logger log = LoggerFactory.getLogger(BeanContextBuilder.class);

  private boolean shutdownHook = true;

  @SuppressWarnings("rawtypes")
  private final List<SuppliedBean> suppliedBeans = new ArrayList<>();

  @SuppressWarnings("rawtypes")
  private final List<EnrichBean> enrichBeans = new ArrayList<>();

  private final Set<String> includeModules = new LinkedHashSet<>();

  private boolean ignoreMissingModuleDependencies;

  /**
   * Create a BeanContextBuilder to ultimately load and return a new BeanContext.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .build()) {
   *
   *     String makeIt = context.getBean(CoffeeMaker.class).makeIt();
   *   }
   * }</pre>
   */
  public BeanContextBuilder() {
  }

  /**
   * Boot the bean context without registering a shutdown hook.
   * <p>
   * The expectation is that the BeanContextBuilder is closed via code or via using
   * try with resources.
   * </p>
   * <pre>{@code
   *
   *   // automatically closed via try with resources
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withNoShutdownHook()
   *     .build()) {
   *
   *     String makeIt = context.getBean(CoffeeMaker.class).makeIt();
   *   }
   *
   * }</pre>
   *
   * @return This BeanContextBuilder
   */
  public BeanContextBuilder withNoShutdownHook() {
    this.shutdownHook = false;
    return this;
  }

  /**
   * Specify the modules to include in dependency injection.
   * <p/>
   * This is effectively a "whitelist" of modules names to include in the injection excluding
   * any other modules that might otherwise exist in the classpath.
   * <p/>
   * We typically want to use this in component testing where we wish to exclude any other
   * modules that exist on the classpath.
   *
   * <pre>{@code
   *
   *   @Test
   *   public void someComponentTest() {
   *
   *     EmailServiceApi mockEmailService = mock(EmailServiceApi.class);
   *
   *     try (BeanContext context = new BeanContextBuilder()
   *       .withBeans(mockEmailService)
   *       .withModules("coffee")
   *       .withIgnoreMissingModuleDependencies()
   *       .build()) {
   *
   *       // built with test doubles injected ...
   *       CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *       coffeeMaker.makeIt();
   *
   *       assertThat(...
   *     }
   *   }
   *
   * }</pre>
   *
   * @param modules The names of modules that we want to include in dependency injection.
   * @return This BeanContextBuilder
   */
  public BeanContextBuilder withModules(String... modules) {
    this.includeModules.addAll(Arrays.asList(modules));
    return this;
  }

  /**
   * Set this when building a BeanContext (typically for testing) and supplied beans replace module dependencies.
   * This means we don't need the usual module dependencies as supplied beans are used instead.
   */
  public BeanContextBuilder withIgnoreMissingModuleDependencies() {
    this.ignoreMissingModuleDependencies = true;
    return this;
  }

  /**
   * Supply a bean to the context that will be used instead of any
   * similar bean in the context.
   * <p>
   * This is typically expected to be used in tests and the bean
   * supplied is typically a test double or mock.
   * </p>
   *
   * <pre>{@code
   *
   *   Pump pump = mock(Pump.class);
   *   Grinder grinder = mock(Grinder.class);
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withBeans(pump, grinder)
   *     .build()) {
   *
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     Pump pump1 = context.getBean(Pump.class);
   *     Grinder grinder1 = context.getBean(Grinder.class);
   *
   *     assertThat(pump1).isSameAs(pump);
   *     assertThat(grinder1).isSameAs(grinder);
   *
   *     verify(pump).pumpWater();
   *     verify(grinder).grindBeans();
   *   }
   *
   * }</pre>
   *
   * @param beans The bean used when injecting a dependency for this bean or the interface(s) it implements
   * @return This BeanContextBuilder
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public BeanContextBuilder withBeans(Object... beans) {
    for (Object bean : beans) {
      suppliedBeans.add(new SuppliedBean(suppliedType(bean.getClass()), bean));
    }
    return this;
  }

  /**
   * Add a supplied bean instance with the given injection type.
   * <p>
   * This is typically a test double often created by Mockito or similar.
   * </p>
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withBean(Pump.class, mock)
   *     .build()) {
   *
   *     Pump pump = context.getBean(Pump.class);
   *     assertThat(pump).isSameAs(mock);
   *
   *     // act
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     verify(pump).pumpSteam();
   *     verify(pump).pumpWater();
   *   }
   *
   * }</pre>
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use (typically a test mock)
   */
  public <D> BeanContextBuilder withBean(Class<D> type, D bean) {
    suppliedBeans.add(new SuppliedBean<>(type, bean));
    return this;
  }

  /**
   * Use a mockito mock when injecting this bean type.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withMock(Pump.class)
   *     .withMock(Grinder.class, grinder -> {
   *       // setup the mock
   *       when(grinder.grindBeans()).thenReturn("stub response");
   *     })
   *     .build()) {
   *
   *
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     // this is a mockito mock
   *     Grinder grinder = context.getBean(Grinder.class);
   *     verify(grinder).grindBeans();
   *   }
   *
   * }</pre>
   */
  public BeanContextBuilder withMock(Class<?> type) {
    return withMock(type, null);
  }

  /**
   * Use a mockito mock when injecting this bean type additionally
   * running setup on the mock instance.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withMock(Pump.class)
   *     .withMock(Grinder.class, grinder -> {
   *
   *       // setup the mock
   *       when(grinder.grindBeans()).thenReturn("stub response");
   *     })
   *     .build()) {
   *
   *
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     // this is a mockito mock
   *     Grinder grinder = context.getBean(Grinder.class);
   *     verify(grinder).grindBeans();
   *   }
   *
   * }</pre>
   */
  public <D> BeanContextBuilder withMock(Class<D> type, Consumer<D> consumer) {
    suppliedBeans.add(new SuppliedBean<>(type, null, consumer));
    return this;
  }

  /**
   * Use a mockito spy when injecting this bean type.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withSpy(Pump.class)
   *     .build()) {
   *
   *     // setup spy here ...
   *     Pump pump = context.getBean(Pump.class);
   *     doNothing().when(pump).pumpSteam();
   *
   *     // act
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     verify(pump).pumpWater();
   *     verify(pump).pumpSteam();
   *   }
   *
   * }</pre>
   */
  public BeanContextBuilder withSpy(Class<?> type) {
    return withSpy(type, null);
  }

  /**
   * Use a mockito spy when injecting this bean type additionally
   * running setup on the spy instance.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BeanContextBuilder()
   *     .withSpy(Pump.class, pump -> {
   *       // setup the spy
   *       doNothing().when(pump).pumpWater();
   *     })
   *     .build()) {
   *
   *     // or setup here ...
   *     Pump pump = context.getBean(Pump.class);
   *     doNothing().when(pump).pumpSteam();
   *
   *     // act
   *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     verify(pump).pumpWater();
   *     verify(pump).pumpSteam();
   *   }
   *
   * }</pre>
   */
  public <D> BeanContextBuilder withSpy(Class<D> type, Consumer<D> consumer) {
    enrichBeans.add(new EnrichBean<>(type, consumer));
    return this;
  }

  /**
   * Build and return the bean context.
   *
   * @return The BeanContext
   */
  public BeanContext build() {
    // sort factories by dependsOn
    FactoryOrder factoryOrder = new FactoryOrder(includeModules, !suppliedBeans.isEmpty(), ignoreMissingModuleDependencies);
    ServiceLoader.load(BeanContextFactory.class).forEach(factoryOrder::add);

    Set<String> moduleNames = factoryOrder.orderFactories();
    if (moduleNames.isEmpty()) {
      throw new IllegalStateException("No modules found suggests using Gradle and IDEA but with a setup issue?" +
        " Review IntelliJ Settings / Build / Build tools / Gradle - 'Build and run using' value and set that to 'Gradle'. " +
        " Refer to https://dinject.io/docs/gradle#idea");
    }
    log.debug("building context with modules {}", moduleNames);
    Builder rootBuilder = BuilderFactory.newRootBuilder(suppliedBeans, enrichBeans);
    for (BeanContextFactory factory : factoryOrder.factories()) {
      rootBuilder.addChild(factory.createContext(rootBuilder));
    }

    BeanContext beanContext = rootBuilder.build();
    // entire graph built, fire postConstruct
    beanContext.start();
    if (shutdownHook) {
      return new ShutdownAwareBeanContext(beanContext);
    }
    return beanContext;
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
   * Internal shutdown hook.
   */
  private static class Hook extends Thread {

    private final ShutdownAwareBeanContext context;

    Hook(ShutdownAwareBeanContext context) {
      this.context = context;
    }

    @Override
    public void run() {
      context.shutdown();
    }
  }

  /**
   * Proxy that handles shutdown hook registration and de-registration.
   */
  private static class ShutdownAwareBeanContext implements BeanContext {

    private final BeanContext context;
    private final Hook hook;
    private boolean shutdown;

    ShutdownAwareBeanContext(BeanContext context) {
      this.context = context;
      this.hook = new Hook(this);
      Runtime.getRuntime().addShutdownHook(hook);
    }

    @Override
    public String getName() {
      return context.getName();
    }

    @Override
    public String[] getProvides() {
      return context.getProvides();
    }

    @Override
    public String[] getDependsOn() {
      return context.getDependsOn();
    }

    @Override
    public <T> T getBean(Class<T> beanClass) {
      return context.getBean(beanClass);
    }

    @Override
    public <T> T getBean(Class<T> beanClass, String name) {
      return context.getBean(beanClass, name);
    }

    @Override
    public <T> BeanEntry<T> candidate(Class<T> type, String name) {
      return context.candidate(type, name);
    }

    @Override
    public List<Object> getBeansWithAnnotation(Class<?> annotation) {
      return context.getBeansWithAnnotation(annotation);
    }

    @Override
    public <T> List<T> getBeans(Class<T> interfaceType) {
      return context.getBeans(interfaceType);
    }

    @Override
    public <T> List<T> getBeansByPriority(Class<T> interfaceType) {
      return context.getBeansByPriority(interfaceType);
    }

    @Override
    public <T> List<T> sortByPriority(List<T> list) {
      return context.sortByPriority(list);
    }

    @Override
    public void start() {
      context.start();
    }

    @Override
    public void close() {
      synchronized (this) {
        if (!shutdown) {
          Runtime.getRuntime().removeShutdownHook(hook);
        }
        context.close();
      }
    }

    /**
     * Close via shutdown hook.
     */
    void shutdown() {
      synchronized (this) {
        shutdown = true;
        close();
      }
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
