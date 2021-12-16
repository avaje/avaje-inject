package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

  Class<?> target();// default Void.class;

  @Target(ElementType.METHOD)
  @interface Before {
  }

  @Target(ElementType.METHOD)
  @interface Around {
  }
}
