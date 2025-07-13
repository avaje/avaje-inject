package io.avaje.inject;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The <code>Priority</code> annotation can be applied to classes to indicate the wiring priority of
 * a bean to resolve cases where multiple beans of the same type exist.
 *
 * <p>Beans can be returned using other Priority annotation such as <code>
 * jakartak.annotation.Priority
 * </code> or any custom priority annotation that has an <code>int value()</code> attribute.
 *
 * @see BeanScope#listByPriority(Type)
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Priority {
  int value();
}
