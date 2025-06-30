package io.avaje.inject;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * Marks dependencies as an external beans potentially not managed by avaje inject. Compile-time
 * validation will be disabled for types annotated.
 *
 * <p>The external dependency is usually expected to be provided by {@link
 * BeanScopeBuilder#bean(String, Type, Object)} or by other modules in cases where ordering is
 * irregular.
 */
@Documented
@Retention(SOURCE)
@Target({FIELD, PARAMETER, CONSTRUCTOR})
public @interface External {}
