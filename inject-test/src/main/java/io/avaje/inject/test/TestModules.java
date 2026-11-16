package io.avaje.inject.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.avaje.inject.spi.AvajeModule;

/**
 * Limit the wiring of {@code @InjectTest} tests to only these modules, rather than every {@code
 * AvajeModule} found on the test classpath.
 *
 * <p>Put this on any class or package-info in the test sources. It sets the default modules for
 * every test in the module under test, including a plain {@code @InjectTest}. A test can still
 * override it with {@code @InjectTest(modules = ...)}.
 *
 * <p>The test sources need at least one {@code @TestScope} bean, as the default modules are
 * recorded on the generated test scope module. Putting it on a {@code @TestScope} bean or factory,
 * or in a package containing one, is the most robust choice, as it is then also read when only some
 * sources are recompiled.
 *
 * <pre>{@code
 * @TestScope
 * @Factory
 * @TestModules({UtilModule.class, MyClientModule.class})
 * class TestConfig {
 *   ...
 * }
 *
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface TestModules {

  /** The modules to wire for tests. */
  Class<? extends AvajeModule>[] value();
}
