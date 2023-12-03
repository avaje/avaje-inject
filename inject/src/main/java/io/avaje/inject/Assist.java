package io.avaje.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** */
@Target({FIELD, PARAMETER})
@Retention(SOURCE)
public @interface Assist {}
