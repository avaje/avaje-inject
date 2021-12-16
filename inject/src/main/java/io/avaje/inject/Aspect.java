package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta annotation used to define an Aspect.
 * <p>
 * Create an annotation and annotate with {@code @Aspect} to define an aspect annotation. The
 * {@link Aspect#target()} specifies the associated type that implements {@link AspectProvider}.
 * The aspect provider should be a {@code @Singleton} such that registers with <em>avaje-inject</em>.
 * <p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

  /**
   * Specify the {@link AspectProvider} for this aspect.
   */
  Class<?> target();

}
