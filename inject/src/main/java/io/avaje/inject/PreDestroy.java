package io.avaje.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>PreDestroy</code> annotation is used on a method as a callback notification
 * to signal that the instance is in the process of being removed by the container.
 * <p>
 * Note that we can equally use any <code>PreDestroy</code> annotation - so we can use
 * the one from <code>javax.annotation</code>, <code>jakarta.annotation</code> or this one.
 * <p>
 * The method annotated with <code>PreDestroy</code> is typically used to release resources
 * that it has been holding.
 * <p>
 * The method on which the <code>PreDestroy</code> annotation is applied must fulfill the
 * following criteria:
 * <ul>
 * <li>The method must not have any parameters.</li>
 * <li>The method may be public, protected or package private.</li>
 * <li>The method must not be static.</li>
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface PreDestroy {
}
