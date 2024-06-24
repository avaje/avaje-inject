package io.avaje.inject.spi;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marker annotation that generates META-INF entries for {@link InjectExtension} Classes */
@Target(TYPE)
@Retention(SOURCE)
public @interface InjectSpi {}
