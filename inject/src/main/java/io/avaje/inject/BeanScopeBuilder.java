package io.avaje.inject;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.PropertyRequiresPlugin;
import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

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
   * Set the PropertyPlugin used for this scope. This is serviceloaded automatically of not set
   *
   * @param propertyRequiresPlugin The plugin for conditions based on properties
   */
  void propertyPlugin(PropertyRequiresPlugin propertyRequiresPlugin);

  /**
   * Return the PropertyPlugin used for this scope. This is useful for plugins that want to use
   * the scopes wiring properties.
   */
  PropertyRequiresPlugin propertyPlugin();

  /**
   * Supply a bean to the scope that will be used instead of any similar bean in the scope.
   *
   * <p>This is typically expected to be used in tests and the bean supplied is typically a test
   * double or mock.
   *
   * <p>If using this to provide a missing bean into the scope, Avaje will fail compilation unless
   * it detects an {@code @InjectModule(requires)} including the missing class, or it detects a
   * {@code @Nullable} annotation where the bean is wired.
   *
   * <pre>{@code
   * // external dependencies
   * Pump pump = ...
   * Grinder grinder = ...
   *
   * BeanScope scope = BeanScope.builder()
   *   .beans(pump, grinder)
   *   .build();
   *
   * CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   * coffeeMaker.makeIt();
   *
   * }</pre>
   *
   * @param beans Externally provided beans used when injecting a dependency for the bean or the
   *     interface(s) it implements
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder beans(Object... beans);

  /**
   * Add a supplied bean instance with the given injection type (typically an interface type).
   *
   * <p>If using this to provide a missing bean into the scope, Avaje will fail compilation unless
   * it detects an {@code @InjectModule(requires)} including the missing class, or it detects a
   * {@code @Nullable} annotation where the bean is wired.
   *
   * <pre>{@code
   * Pump externalDependency = ...
   *
   * try (BeanScope scope = BeanScope.builder()
   *   .bean(Pump.class, externalDependency)
   *   .build()) {
   *
   *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *   coffeeMaker.makeIt();
   *
   *   Pump pump = scope.get(Pump.class);
   *   assertThat(pump).isSameAs(externalDependency);
   * }
   *
   * }</pre>
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and injection type.
   *
   * <p>If using this to provide a missing bean into the scope, Avaje will fail compilation unless
   * it detects an {@code @InjectModule(requires)} including the missing class, or it detects a
   * {@code @Nullable} annotation where the bean is wired.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(String name, Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and generic type.
   *
   * <p>If using this to provide a missing bean into the scope, Avaje will fail compilation unless
   * it detects an {@code @InjectModule(requires)} including the missing class, or it detects a
   * {@code @Nullable} annotation where the bean is wired.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(String name, Type type, D bean);

  /**
   * Add a supplied bean instance with a generic type.
   *
   * <p>If using this to provide a missing bean into the scope, Avaje will fail compilation unless
   * it detects an {@code @InjectModule(requires)} including the missing class, or it detects a
   * {@code @Nullable} annotation where the bean is wired.
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder bean(Type type, D bean);

  /**
   * Add a supplied bean provider that acts as a default fallback for a dependency.
   * <p>
   * This provider is only called if nothing else provides the dependency. It effectively
   * uses `@Secondary` priority.
   *
   * @param type     The type of the dependency
   * @param provider The provider of the dependency.
   */
  default <D> BeanScopeBuilder provideDefault(Type type, Supplier<D> provider) {
    return provideDefault(null, type, provider);
  }

  /**
   * Add a supplied bean provider that acts as a default fallback for a dependency.
   * <p>
   * This provider is only called if nothing else provides the dependency. It effectively
   * uses `@Secondary` priority.
   *
   * @param name     The name qualifier
   * @param type     The type of the dependency
   * @param provider The provider of the dependency.
   */
  <D> BeanScopeBuilder provideDefault(@Nullable String name, Type type, Supplier<D> provider);

  /**
   * Adds hooks that will execute after this scope is built.
   *
   * @param runnables the PostConstruct hooks to run after the BeanScope is constructed
   */
  BeanScopeBuilder addPostConstructHooks(Runnable... runnables);

  /**
   * Adds hooks that will execute after this scope is built.
   *
   * @param consumers the PostConstruct hooks to run after the BeanScope is constructed
   */
  @SuppressWarnings("unchecked")
  BeanScopeBuilder addPostConstructHooks(Consumer<BeanScope>... consumers);

  /**
   * Adds hooks that will execute before this scope is destroyed.
   *
   * @param closables the PreDestroy hooks to add to the bean scope
   */
  BeanScopeBuilder addPreDestroyHooks(AutoCloseable... closables);

  /**
   * Set the ClassLoader to use when loading modules.
   *
   * @param classLoader The ClassLoader to use
   */
  BeanScopeBuilder classLoader(ClassLoader classLoader);

  /**
   * Use the given BeanScope as the parent. This becomes an additional
   * source of beans that can be wired and accessed in this scope.
   *
   * @param parent The BeanScope that acts as the parent
   */
  BeanScopeBuilder parent(BeanScope parent);

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
  }
}
