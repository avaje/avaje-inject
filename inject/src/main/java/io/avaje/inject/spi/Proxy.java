package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks the type as being a Proxy. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Proxy {}
