package io.dinject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identify a bean as being the least preferred bean to inject when multiple beans implement
 * the intended interface.
 * <p>
 * This can use be used when we have a 'default' implementation that would only be used when
 * no other implementation is available in the context to inject.
 * </p>
 *
 * <pre>{@code
 *
 * @Secondary
 * @Singleton
 * class DefaultEmailSender implements EmailSender {
 *
 *   ...
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secondary {
}
