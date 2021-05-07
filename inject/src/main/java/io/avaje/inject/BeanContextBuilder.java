package io.avaje.inject;

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
 *     try (BeanContext context = BeanContext.newBuilder()
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
public interface BeanContextBuilder {

  /**
   * Create the bean context without registering a shutdown hook.
   * <p>
   * The expectation is that the BeanContextBuilder is closed via code or via using
   * try with resources.
   * </p>
   * <pre>{@code
   *
   *   // automatically closed via try with resources
   *
   *   try (BeanContext context = BeanContext.newBuilder()
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
  BeanContextBuilder withNoShutdownHook();

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
   *     try (BeanContext context = BeanContext.newBuilder()
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
  BeanContextBuilder withModules(String... modules);

  /**
   * Set this when building a BeanContext (typically for testing) and supplied beans replace module dependencies.
   * This means we don't need the usual module dependencies as supplied beans are used instead.
   */
  BeanContextBuilder withIgnoreMissingModuleDependencies();

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
   *   try (BeanContext context = BeanContext.newBuilder()
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
  BeanContextBuilder withBeans(Object... beans);

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
   *   try (BeanContext context = BeanContext.newBuilder()
   *     .withBean(Pump.class, mockPump)
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
  <D> BeanContextBuilder withBean(Class<D> type, D bean);

  /**
   * Add a supplied bean instance with the given name and injection type.
   *
   * @param name The name qualifier
   * @param type The dependency injection type this bean is target for
   * @param bean The supplied bean instance to use (typically a test mock)
   */
  <D> BeanContextBuilder withBean(String name, Class<D> type, D bean);

  /**
   * Use a mockito mock when injecting this bean type.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = BeanContext.newBuilder()
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
  BeanContextBuilder withMock(Class<?> type);

  /**
   * Use a mockito mock when injecting this bean type additionally
   * running setup on the mock instance.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = BeanContext.newBuilder()
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
  <D> BeanContextBuilder withMock(Class<D> type, Consumer<D> consumer);

  /**
   * Use a mockito spy when injecting this bean type.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = BeanContext.newBuilder()
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
  BeanContextBuilder withSpy(Class<?> type);

  /**
   * Use a mockito spy when injecting this bean type additionally
   * running setup on the spy instance.
   *
   * <pre>{@code
   *
   *   try (BeanContext context = BeanContext.newBuilder()
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
  <D> BeanContextBuilder withSpy(Class<D> type, Consumer<D> consumer);

  /**
   * Build and return the bean context.
   *
   * @return The BeanContext
   */
  BeanContext build();
}
