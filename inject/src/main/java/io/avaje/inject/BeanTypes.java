package io.avaje.inject;

import java.lang.annotation.*;

/** Limits the types exposed by this bean to the given types. */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface BeanTypes {

  /** The types the component will register to. */
  Class<?>[] value();

  /**
   * Extra types to register the component to that are not included in the
   * <code>isBeanAbsent()</code> check. For testing purposes, when providing
   * test doubles, these types are not checked.
   */
  Class<?>[] registerTypes() default {};

}
