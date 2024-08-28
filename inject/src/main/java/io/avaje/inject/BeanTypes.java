package io.avaje.inject;

import java.lang.annotation.*;

/** Limits the types exposed by this bean to the given types. */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface BeanTypes {

  Class<?>[] value();
}
