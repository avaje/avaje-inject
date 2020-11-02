package io.avaje.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>PostConstruct</code> annotation is used on a method that needs to be executed
 * after dependency injection is done to perform any initialization.
 * <p>
 * Note that we can equally use any <code>PostConstruct</code> annotation - so we can use
 * the one from <code>javax.annotation</code>, <code>jakarta.annotation</code> or this one.
 * </p>
 * <p>
 * Only one method in a given class can be annotated with this annotation.
 * <p>
 * The method on which the <code>PostConstruct</code> annotation is applied must fulfill
 * the following criteria:
 * <ul>
 * <li>The method must not have any parameters.</li>
 * <li>The method may be public, protected or package private.</li>
 * <li>The method must not be static.</li>
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface PostConstruct {
}
