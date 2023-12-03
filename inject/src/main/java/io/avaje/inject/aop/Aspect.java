package io.avaje.inject.aop;

import java.lang.annotation.*;

/**
 * Meta annotation used to define an Aspect.
 *
 * <p>Create an annotation and annotate with {@code @Aspect} to define an aspect annotation. The
 * associated type that implements {@link AspectProvider} will be used as the target class. The
 * aspect provider should be a {@code @Singleton} such that registers with <em>avaje-inject</em>.
 *
 * <p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

  /**
   * Specify the priority ordering when multiple aspects are on a method.
   *
   * <p>When multiple aspects are on a method they are nested. The highest ordering value will be
   * the outer-most aspect, the lowest ordering will be the inner-most aspect.
   *
   * <p>The outer-most aspect will have it's <em>before</em> executed first, followed by the
   * <em>before</em> of the inner nested aspects ultimately down the invocation of the target
   * method.
   *
   * <p>The reverse ordering occurs for <em>after</em> with the outer-most aspect having it's
   * <em>after</em> executed last.
   *
   * @return The ordering of this aspect. High value for outer-most aspect.
   */
  int ordering() default 1000;

  /**
   * Marks an External Annotation as being used for aspects
   */
  @Target({ElementType.PACKAGE, ElementType.TYPE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Import {

    Class<? extends Annotation> value();

    /**
     * Specify the priority ordering when multiple aspects are on a method.
     *
     * <p>When multiple aspects are on a method they are nested. The highest ordering value will be
     * the outer-most aspect, the lowest ordering will be the inner-most aspect.
     *
     * <p>The outer-most aspect will have it's <em>before</em> executed first, followed by the
     * <em>before</em> of the inner nested aspects ultimately down the invocation of the target
     * method.
     *
     * <p>The reverse ordering occurs for <em>after</em> with the outer-most aspect having it's
     * <em>after</em> executed last.
     *
     * @return The ordering of this aspect. High value for outer-most aspect.
     */
    int ordering() default 1000;
  }
}
