package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identify a bean as being the preferred bean to inject when multiple beans implement
 * the intended interface.
 *
 * <pre>{@code
 *
 * ＠Primary
 * ＠Singleton
 * class PreferredEmailSender implements EmailSender {
 *
 *   ...
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
