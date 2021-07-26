package io.avaje.inject;

import io.avaje.inject.spi.Module;

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
 *   BeanScope scope = BeanScope.newBuilder()
 *     .withBean(pump)
 *     .build();
 *
 *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
 *   coffeeMaker.makeIt();
 *
 * }</pre>
 */
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
   *   BeanScope scope = BeanScope.newBuilder()
   *     .withShutdownHook(true)
   *     .build());
   *
   *   // on JVM shutdown the preDestroy lifecycle methods are executed
   *
   * }</pre>
   *
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder withShutdownHook(boolean shutdownHook);

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
   *   BeanScope scope = BeanScope.newBuilder()
   *     .withModules(new CustomModule())
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
  BeanScopeBuilder withModules(Module... modules);

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
   *   BeanScope scope = BeanScope.newBuilder()
   *     .withBeans(pump, grinder)
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
  BeanScopeBuilder withBeans(Object... beans);

  /**
   * Add a supplied bean instance with the given injection type (typically an interface type).
   *
   * <pre>{@code
   *
   *   Pump externalDependency = ...
   *
   *   try (BeanScope scope = BeanScope.newBuilder()
   *     .withBean(Pump.class, externalDependency)
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
  <D> BeanScopeBuilder withBean(Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and injection type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder withBean(String name, Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and generic type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder withBean(String name, Type type, D bean);

  /**
   * Add a supplied bean instance with a generic type.
   *
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use for injection
   */
  <D> BeanScopeBuilder withBean(Type type, D bean);

  /**
   * Use the given BeanScope as the parent. This becomes an additional
   * source of beans that can be wired and accessed in this scope.
   *
   * @param parent The BeanScope that acts as the parent
   */
  BeanScopeBuilder withParent(BeanScope parent);

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
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withMock(Pump.class)
     *     .withMock(Grinder.class)
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
    BeanScopeBuilder.ForTesting withMock(Class<?> type);

    /**
     * Register as a Mockito mock with a qualifier name.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withMock(Store.class, "red")
     *     .withMock(Store.class, "blue")
     *     .build()) {
     *
     *     ...
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting withMock(Class<?> type, String name);

    /**
     * Use a mockito mock when injecting this bean type additionally
     * running setup on the mock instance.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withMock(Pump.class)
     *     .withMock(Grinder.class, grinder -> {
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
    <D> BeanScopeBuilder.ForTesting withMock(Class<D> type, Consumer<D> consumer);

    /**
     * Use a mockito spy when injecting this bean type.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withSpy(Pump.class)
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
    BeanScopeBuilder.ForTesting withSpy(Class<?> type);

    /**
     * Register a Mockito spy with a qualifier name.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withSpy(Store.class, "red")
     *     .withSpy(Store.class, "blue")
     *     .build()) {
     *
     *     ...
     *   }
     *
     * }</pre>
     */
    BeanScopeBuilder.ForTesting withSpy(Class<?> type, String name);

    /**
     * Use a mockito spy when injecting this bean type additionally
     * running setup on the spy instance.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
     *     .forTesting()
     *     .withSpy(Pump.class, pump -> {
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
    <D> BeanScopeBuilder.ForTesting withSpy(Class<D> type, Consumer<D> consumer);

  }
}
