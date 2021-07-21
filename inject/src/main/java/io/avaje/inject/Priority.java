package io.avaje.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>Priority</code> annotation can be applied to classes to indicate
 * in what order they should be returned via @{@link BeanScope#listByPriority(Class)}.
 * <p>
 * Beans can be returned using other Priority annotation such as <code>javax.annotation.Priority</code>
 * or any custom priority annotation that has an <code>int value()</code> attribute.
 * </p>
 *
 * @see BeanScope#listByPriority(Class)
 * @see BeanScope#listByPriority(Class, Class)
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Priority {
  int value();
}
