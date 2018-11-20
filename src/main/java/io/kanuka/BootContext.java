package io.kanuka;

import io.kanuka.core.BeanContextFactory;
import io.kanuka.core.Builder;
import io.kanuka.core.BuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Boot and create a bean context with options for shutdown hook and supplying test doubles.
 * <p>
 * We would choose to use BootContext in test code (for component testing) as it gives us
 * the ability to inject test doubles etc.
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
 *     try (BeanContext context = new BootContext()
 *       .withBean(mockRedis)
 *       .withBean(mockDatabase)
 *       .load()) {
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
public class BootContext {

  private static final Logger log = LoggerFactory.getLogger(BootContext.class);

  private boolean shutdownHook = true;

  private final List<Object> suppliedBeans = new ArrayList<>();

  private final Set<String> modules = new LinkedHashSet<>();

  /**
   * Create a BootContext to ultimately load and return a new BeanContext.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = new BootContext()
   *     .load()) {
   *
   *     String makeIt = context.getBean(CoffeeMaker.class).makeIt();
   *   }
   * }</pre>
   */
  public BootContext() {
  }

  /**
   * Boot the bean context without registering a shutdown hook.
   * <p>
   * The expectation is that the BootContext is closed via code or via using
   * try with resources.
   * </p>
   * <pre>{@code
   *
   *   // automatically closed via try with resources
   *
   *   try (BeanContext context = new BootContext()
   *     .withNoShutdownHook()
   *     .load()) {
   *
   *     String makeIt = context.getBean(CoffeeMaker.class).makeIt();
   *   }
   *
   * }</pre>
   *
   * @return This BootContext
   */
  public BootContext withNoShutdownHook() {
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
   *     try (BeanContext context = new BootContext()
   *       .withBeans(mockEmailService)
   *       .withModules("coffee")
   *       .load()) {
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
   *
   * @param modules The names of modules that we want to include in dependency injection.
   * @return This BootContext
   */
  public BootContext withModules(String... modules) {
    this.modules.addAll(Arrays.asList(modules));
    return this;
  }

  /**
   * Supply a bean to the context that will be used instead of any similar bean in the context.
   * <p>
   * This is typically expected to be used in tests and the bean supplied is typically a test double
   * or mock.
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
   *     try (BeanContext context = new BootContext()
   *       .withBeans(mockRedis, mockDatabase)
   *       .load()) {
   *
   *       // built with test doubles injected ...
   *       CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
   *       coffeeMaker.makeIt();
   *
   *       assertThat(...
   *     }
   *   }
   *
   *
   * }</pre>
   *
   * @param beans The bean used when injecting a dependency for this bean or the interface(s) it implements
   * @return This BootContext
   */
  public BootContext withBeans(Object... beans) {
    suppliedBeans.addAll(Arrays.asList(beans));
    return this;
  }

  /**
   * Build and return the bean context.
   */
  public BeanContext load() {

    // sort factories by dependsOn
    FactoryOrder factoryOrder = new FactoryOrder(modules);
    ServiceLoader.load(BeanContextFactory.class).forEach(factoryOrder::add);

    Set<String> moduleNames = factoryOrder.orderFactories();
    log.debug("building context with modules {}", moduleNames);

    Builder rootBuilder = BuilderFactory.newRootBuilder(suppliedBeans);

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
    public List<Object> getBeansWithAnnotation(Class<?> annotation) {
      return context.getBeansWithAnnotation(annotation);
    }

    @Override
    public <T> List<T> getBeans(Class<T> interfaceType) {
      return context.getBeans(interfaceType);
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

    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<BeanContextFactory> factories = new ArrayList<>();
    private final List<BeanContextFactory> queue = new ArrayList<>();

    FactoryOrder(Set<String> includeModules) {
      this.includeModules = includeModules;
    }

    void add(BeanContextFactory factory) {

      if (includeModule(factory)) {
        String[] dependsOn = factory.getDependsOn();
        if (dependsOn == null || dependsOn.length == 0) {
          push(factory);
        } else {
          queue.add(factory);
        }
      }
    }

    /**
     * Return true of the factory (for the module) should be included.
     */
    private boolean includeModule(BeanContextFactory factory) {
      return includeModules.isEmpty() || includeModules.contains(factory.getName());
    }

    private void push(BeanContextFactory factory) {
      factories.add(factory);
      moduleNames.add(factory.getName());
    }

    /**
     * Order the factories returning the ordered list of module names.
     */
    Set<String> orderFactories() {
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

      if (!queue.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (BeanContextFactory factory : queue) {
          sb.append("module ").append(factory.getName()).append(" has unsatisfied dependencies - ");
          for (String depModuleName : factory.getDependsOn()) {
            boolean ok = moduleNames.contains(depModuleName);
            String result = (ok) ? "ok" : "UNSATISFIED";
            sb.append(String.format("depends on %s - %s", depModuleName, result));
          }
        }

        sb.append("- Modules loaded ok ").append(moduleNames);
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
      Iterator<BeanContextFactory> it = queue.iterator();
      while (it.hasNext()) {
        BeanContextFactory factory = it.next();
        if (satisfiedDependencies(factory)) {
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
    private boolean satisfiedDependencies(BeanContextFactory factory) {
      for (String moduleName : factory.getDependsOn()) {
        if (!moduleNames.contains(moduleName)) {
          return false;
        }
      }
      return true;
    }
  }

}
