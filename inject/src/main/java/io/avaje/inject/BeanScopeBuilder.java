package io.avaje.inject;

import io.avaje.inject.spi.Module;

import java.util.function.Consumer;

/**
 * Build a bean scope with options for shutdown hook and supplying test doubles.
 * <p>
 * We would choose to use BeanScopeBuilder in test code (for component testing) as it gives us
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
 *     try (BeanScope scope = BeanScope.newBuilder()
 *       .withBeans(mockRedis, mockDatabase)
 *       .build()) {
 *
 *       // built with test doubles injected ...
 *       CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
 *       coffeeMaker.makeIt();
 *
 *       assertThat(...
 *     }
 *   }
 *
 * }</pre>
 */
public interface BeanScopeBuilder {

  /**
   * Create the bean scope registering a shutdown hook (defaults to false, no shutdown hook).
   * <p>
   * The expectation is that the BeanScopeBuilder is closed via code or via using
   * try with resources.
   * </p>
   * <pre>{@code
   *
   *   // automatically closed via try with resources
   *
   *   try (BeanScope scope = BeanScope.newBuilder()
   *     .withShutdownHook(true)
   *     .build()) {
   *
   *     String makeIt = scope.get(CoffeeMaker.class).makeIt();
   *   }
   *
   * }</pre>
   *
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder withShutdownHook(boolean shutdownHook);

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
   *     try (BeanScope scope = BeanScope.newBuilder()
   *       .withBeans(mockEmailService)
   *       .withModules("coffee")
   *       .withIgnoreMissingModuleDependencies()
   *       .build()) {
   *
   *       // built with test doubles injected ...
   *       CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *       coffeeMaker.makeIt();
   *
   *       assertThat(...
   *     }
   *   }
   *
   * }</pre>
   *
   * @param modules The names of modules that we want to include in dependency injection.
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
   *   Pump pump = mock(Pump.class);
   *   Grinder grinder = mock(Grinder.class);
   *
   *   try (BeanScope scope = BeanScope.newBuilder()
   *     .withBeans(pump, grinder)
   *     .build()) {
   *
   *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *     coffeeMaker.makeIt();
   *
   *     Pump pump1 = scope.get(Pump.class);
   *     Grinder grinder1 = scope.get(Grinder.class);
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
   * @return This BeanScopeBuilder
   */
  BeanScopeBuilder withBeans(Object... beans);

  /**
   * Add a supplied bean instance with the given injection type.
   * <p>
   * This is typically a test double often created by Mockito or similar.
   * </p>
   *
   * <pre>{@code
   *
   *   Pump mockPump = ...
   *
   *   try (BeanScope scope = BeanScope.newBuilder()
   *     .withBean(Pump.class, mockPump)
   *     .build()) {
   *
   *     Pump pump = scope.get(Pump.class);
   *     assertThat(pump).isSameAs(mock);
   *
   *     // act
   *     CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
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
  <D> BeanScopeBuilder withBean(Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and injection type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use (typically a test mock)
   */
  <D> BeanScopeBuilder withBean(String name, Class<D> type, D bean);

  /**
   * Extend the builder to support testing methods <code>withMock()</code> and <code>withSpy()</code>
   *
   * @return The builder with extra testing support for mocks and spies
   */
  BeanScopeBuilder.ForTesting forTesting();

  /**
   * Build and return the bean scope.
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
     *     .withMock(Pump.class)
     *     .withMock(Grinder.class, grinder -> {
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
    BeanScopeBuilder.ForTesting withMock(Class<?> type);

    /**
     * Register as a Mockito mock with a qualifier name.
     *
     * <pre>{@code
     *
     *   try (BeanScope scope = BeanScope.newBuilder()
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
