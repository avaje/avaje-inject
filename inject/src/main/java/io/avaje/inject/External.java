package io.avaje.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * Marks this dependency as an external bean not managed by avaje inject.
 * Compile-time validation will be disabled for this type.
 * <p>
 * The external dependency is expected to be provided by
 * {@link BeanScopeBuilder#bean(String, Type, Object)}.
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface External {}
