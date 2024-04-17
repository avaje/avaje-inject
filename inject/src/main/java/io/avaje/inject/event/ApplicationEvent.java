package io.avaje.inject.event;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a class/record as an event class, signaling the annotation processor to generate a
 * publisher class for the annotated type.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface ApplicationEvent {}
