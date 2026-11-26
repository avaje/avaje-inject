package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holds instance injector registration and dependency metadata for partial compilation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Repeatable(InstanceInjectorMeta.Container.class)
public @interface InstanceInjectorMeta {

  /** The target type for the generated instance injector. */
  String target();

  /** The generated injector implementation type. */
  String injector();

  /** The dependencies required by the target's injected fields and methods. */
  String[] dependsOn() default {};

  /** Container annotation for repeatable {@link InstanceInjectorMeta}. */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  @interface Container {

    InstanceInjectorMeta[] value();
  }
}
