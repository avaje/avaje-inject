package io.avaje.inject.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.avaje.inject.spi.AvajeModule;

/**
 * An avaje-inject test supporting {@code @Inject} along with Mockito annotations -
 * {@code @Mock, @Spy, @Captor}.
 *
 * <p>This is a JUnit 5 extension.
 */
@ExtendWith(InjectJunitExtension.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectTest {

  /** Wiring profiles to use */
  String[] profiles() default {};

  /** Create a new test beanscope for each test method */
  boolean scopePerTest() default false;

  /**
   * Limit the wiring to only these modules, rather than every {@code AvajeModule} found on the test
   * classpath.
   *
   * <p>This is the equivalent of {@code BeanScope.builder().modules(...)}
   */
  Class<? extends AvajeModule>[] modules() default {};
}
