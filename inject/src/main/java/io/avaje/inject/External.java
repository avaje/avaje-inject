package io.avaje.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks this dependency as an external bean not managed by avaje inject. Compile-time validation
 * will be disabled for this type.
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface External {}
