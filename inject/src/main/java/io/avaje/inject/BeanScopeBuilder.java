package io.avaje.inject;

import io.avaje.inject.spi.Module;
import io.avaje.lang.NonNullApi;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Build a bean scope with options for shutdown hook and supplying external dependencies.
 * <p>
 * We can provide external dependencies that are then used in wiring the components.
 * </p>
 *
 * <pre>{@code
 *
 *   // external dependencies
 *   Pump pump = ...
 *
 *   BeanScope scope = BeanScope.builder()
 *     .bean(pump)
 *     .build();
 *
 *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
 *   coffeeMaker.makeIt();
 *
 * }</pre>
 */
@NonNullApi
public interface BeanScopeBuilder {

  /**
   * Create the bean scope registering a shutdown hook (defaults to false, no shutdown hook).
   * <p>
   * With {@code withShutdownHook(true)} a shutdown hook will be registered with the Runtime
   * and executed when the JVM initiates a shutdown. This then will run the {@code preDestroy}
   * lifecycle methods.
   * </p>
   * <pre>{@code
   *
   *   // automatically closed via try with resources
   *
   *   BeanScope scope = BeanScope.builder()
   *     .shutdownHook(true)
   *     .build());
   *
   *   // on JVM shutdown the preDestroy lifecycle methods are executed
   *
   * }</pre>
   *
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder shutdownHook(boolean shutdownHook);

  /**
   * Deprecated - migrate to shutdownHook().
   */
  @Deprecated
  default BeanScopeBuilder withShutdownHook(boolean shutdownHook) {
    return shutdownHook(shutdownHook);
  }

  /**
   * Specify the modules to include in dependency injection.
   * <p>
   * Only beans related to the module are included in the BeanScope that is built.
   * <p>
   * When we do not explicitly specify modules then all modules that are not "custom scoped"
   * are found and used via service loading.
   *
   * <pre>{@code
   *
   *   BeanScope scope = BeanScope.builder()
   *     .modules(new CustomModule())
   *     .build());
   *
   *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *   coffeeMaker.makeIt();
   *
   * }</pre>
   *
   * @param modules The modules that we want to include in dependency injection.
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder modules(Module... modules);

  /**
   * Deprecated - migrate to modules()
   */
  @Deprecated
  default BeanScopeBuilder withModules(Module... modules) {
    return modules(modules);
  }

  /**
   * Supply a bean to the scope that will be used instead of any
   * similar bean in the scope.
   * <p>
   * This is typically expected to be used in tests and the bean
   * supplied is typically a test double or mock.
   * </p>
   *
   * <pre>{@code
   *
   *   // external dependencies
   *   Pump pump = ...
   *   Grinder grinder = ...
   *
   *   BeanScope scope = BeanScope.builder()
   *     .beans(pump, grinder)
   *     .build();
   *
   *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *   coffeeMaker.makeIt();
   *
   * }</pre>
   *
   * @param beans Externally provided beans used when injecting a dependency
   *              for the bean or the interface(s) it implements
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder beans(Object... beans);

  /**
   * Deprecated - migrate to beans().
   */
  @Deprecated
  default BeanScopeBuilder withBeans(Object... beans) {
    return beans(beans);
  }

  /**
   * Add a supplied bean instance with the given injection type (typically an interface type).
   *
   * <pre>{@code
   *
   *   Pump externalDependency = ...
   *
   *   try (BeanScope scope = BeanScope.builder()
   *     .bean(Pump.class, externalDependency)
   *     .build()) {
   *
   *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     Pump pump = scope.get(Pump.class);
   *     assertThat(pump).isSameAs(externalDependency);
   *   }
   *
   * }</pre>
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(Class<D> type, D bean);

  /**
   * Deprecated - migrate to bean().
   */
  @Deprecated
  default <D> BeanScopeBuilder withBean(Class<D> type, D bean) {
    return bean(type, bean);
  }

  /**
   * Add a supplied bean instance with the given name and injection type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(String name, Class<D> type, D bean);

  /**
   * Deprecated - migrate to bean().
   */
  @Deprecated
  default <D> BeanScopeBuilder withBean(String name, Class<D> type, D bean) {
    return bean(name, type, bean);
  }

  /**
   * Add a supplied bean instance with the given name and generic type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(String name, Type type, D bean);

  /**
   * Deprecated - migrate to bean().
   */
  @Deprecated
  default <D> BeanScopeBuilder withBean(String name, Type type, D bean) {
    return bean(name, type, bean);
  }

  /**
   * Add a supplied bean instance with a generic type.
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(Type type, D bean);

  /**
   * Deprecated - migrate to bean().
   */
  @Deprecated
  default <D> BeanScopeBuilder withBean(Type type, D bean) {
    return bean(type, bean);
  }

  /**
   * Use the given BeanScope as the parent. This becomes an additional
   * source of beans that can be wired and accessed in this scope.
   *
   * @param parent The BeanScope that acts as the parent
   */
  BeanScopeBuilder parent(BeanScope parent);

  /**
   * Deprecated - migrate to parent().
   */
  @Deprecated
  default BeanScopeBuilder withParent(BeanScope parent) {
    return parent(parent);
  }

  /**
   * Use the given BeanScope as the parent additionally specifying if beans
   * added will effectively override beans that exist in the parent scope.
   * <p>
   * By default, child scopes will override a bean that exists in a parent scope.
   * For testing purposes, parentOverride=false is used such that bean provided
   * in parent test scopes are used (unless we mock() or spy() them).
   * <p>
   * See TestBeanScope in avaje-inject-test which has helper methods to build
   * BeanScopes for testing with the "Global test scope" as a parent scope.
   *
   * @param parent         The BeanScope that acts as the parent
   * @param parentOverride When false do not add beans that already exist on the parent.
   *                       When true add beans regardless of whether they exist in the parent scope.
   */
  BeanScopeBuilder parent(BeanScope parent, boolean parentOverride);

  /**
   * Deprecated - migrate to parent().
   */
  @Deprecated
  default BeanScopeBuilder withParent(BeanScope parent, boolean parentOverride) {
    return parent(parent, parentOverride);
  }

  /**
   * Extend the builder to support testing using mockito with
   * <code>withMock()</code> and <code>withSpy()</code> methods.
   *
   * @return The builder with extra testing support for mockito mocks and spies
   */
  BeanScopeBuilder.ForTesting forTesting();

  /**
   * Build and return the bean scope.
   * <p>
   * The BeanScope is effectively immutable in that all components are created
   * and all PostConstruct lifecycle methods have been invoked.
   * <p>
   * The beanScope effectively contains eager singletons.
   *
   * @return The BeanScope
   */
  BeanScope build();

  /**
   * Extends the building with testing specific support for mocks and spies.
   */
  interface ForTesting extends BeanScopeBuilder {

    /**
     * Use a mockito mock when injecting this bean type.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .mock(Pump.class)
     *     .mock(Grinder.class)
     *     .build()) {
     *
     *
     *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
     *     coffeeMaker.makeIt();
     *
     *     // this is a mockito mock
     *     Grinder grinder = scope.get(Grinder.class);
     *     verify(grinder).grindBeans();
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting mock(Class<?> type);

    /**
     * Deprecated - migrate to mock().
     */
    @Deprecated
    default BeanScopeBuilder.ForTesting withMock(Class<?> type) {
      return  mock(type);
    }

    /**
     * Register as a Mockito mock with a qualifier name.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .mock(Store.class, "red")
     *     .mock(Store.class, "blue")
     *     .build()) {
     *
     *     ...
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting mock(Class<?> type, String name);

    /**
     * Deprecated - migrate to mock().
     */
    @Deprecated
    default BeanScopeBuilder.ForTesting withMock(Class<?> type, String name) {
      return mock(type, name);
    }

    /**
     * Use a mockito mock when injecting this bean type additionally
     * running setup on the mock instance.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .mock(Pump.class)
     *     .mock(Grinder.class, grinder -> {
     *
     *       // setup the mock
     *       when(grinder.grindBeans()).thenReturn("stub response");
     *     })
     *     .build()) {
     *
     *
     *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
     *     coffeeMaker.makeIt();
     *
     *     // this is a mockito mock
     *     Grinder grinder = scope.get(Grinder.class);
     *     verify(grinder).grindBeans();
     *   }
     *
     * }</pre>
     */
    <D> BeanScopeBuilder.ForTesting mock(Class<D> type, Consumer<D> consumer);

    /**
     * Deprecated - migrate to mock().
     */
    @Deprecated
    default <D> BeanScopeBuilder.ForTesting withMock(Class<D> type, Consumer<D> consumer) {
      return mock(type, consumer);
    }

    /**
     * Use a mockito spy when injecting this bean type.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .spy(Pump.class)
     *     .build()) {
     *
     *     // setup spy here ...
     *     Pump pump = scope.get(Pump.class);
     *     doNothing().when(pump).pumpSteam();
     *
     *     // act
     *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
     *     coffeeMaker.makeIt();
     *
     *     verify(pump).pumpWater();
     *     verify(pump).pumpSteam();
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting spy(Class<?> type);

    /**
     * Deprecated - migrate to spy().
     */
    @Deprecated
    default BeanScopeBuilder.ForTesting withSpy(Class<?> type) {
      return spy(type);
    }

    /**
     * Register a Mockito spy with a qualifier name.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .spy(Store.class, "red")
     *     .spy(Store.class, "blue")
     *     .build()) {
     *
     *     ...
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting spy(Class<?> type, String name);

    /**
     * Deprecated - migrate to spy().
     */
    @Deprecated
    default BeanScopeBuilder.ForTesting withSpy(Class<?> type, String name) {
      return spy(type, name);
    }

    /**
     * Use a mockito spy when injecting this bean type additionally
     * running setup on the spy instance.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.builder()
     *     .forTesting()
     *     .spy(Pump.class, pump -> {
     *       // setup the spy
     *       doNothing().when(pump).pumpWater();
     *     })
     *     .build()) {
     *
     *     // or setup here ...
     *     Pump pump = scope.get(Pump.class);
     *     doNothing().when(pump).pumpSteam();
     *
     *     // act
     *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
     *     coffeeMaker.makeIt();
     *
     *     verify(pump).pumpWater();
     *     verify(pump).pumpSteam();
     *   }
     *
     * }</pre>
     */
    <D> BeanScopeBuilder.ForTesting spy(Class<D> type, Consumer<D> consumer);

    /**
     * Deprecated - migrate to spy().
     */
    @Deprecated
    default <D> BeanScopeBuilder.ForTesting withSpy(Class<D> type, Consumer<D> consumer) {
      return spy(type, consumer);
    }

  }
}
