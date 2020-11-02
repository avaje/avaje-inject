package io.avaje.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>Priority</code> annotation can be applied to classes to indicate
 * in what order they should be returned via @{@link SystemContext#getBeansByPriority(Class)}.
 * <p>
 * Beans can be returned using other Priority annotation such as <code>javax.annotation.Priority</code>
 * or any custom priority annotation that has an <code>int value()</code> attribute.
 * </p>
 *
 * @see BeanContext#getBeansByPriority(Class)
 * @see BeanContext#getBeansByPriority(Class, Class)
 * @see BeanContext#sortByPriority(List, Class)
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Priority {
  int value();
}
