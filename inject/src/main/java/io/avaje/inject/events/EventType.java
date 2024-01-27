package io.avaje.inject.events;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// TODO better name???
/**
 * Marks a class/record as an event class, signaling the annotation processor to generate a
 * publisher class for the annotated type.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface EventType {}
