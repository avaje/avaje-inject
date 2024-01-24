package io.avaje.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used with {@link AssistFactory} to mark the properties that are
 * provided as parameters to the factory method.
 */
@Target({FIELD, PARAMETER})
@Retention(SOURCE)
public @interface Assisted {}
