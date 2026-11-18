package org.example.myapp.customannotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Singleton;

/** Custom annotation that registers beans with singleton scope. */
@Singleton
@Target(TYPE)
@Retention(SOURCE)
public @interface MyService {
}
