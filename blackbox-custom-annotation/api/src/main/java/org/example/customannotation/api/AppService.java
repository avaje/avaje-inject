package org.example.customannotation.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Singleton;

/** Custom annotation registering beans with singleton scope. */
@Singleton
@Target(TYPE)
@Retention(SOURCE)
public @interface AppService {
}
