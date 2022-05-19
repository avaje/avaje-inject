package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a bean that has prototype scope.
 * <p>
 * A new instance of this bean will be created each time it is requested or wired.
 *
 * <pre>{@code
 *
 * @Prototype
 * class EmailSendHandler {
 *
 *   ...
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Prototype {
}
