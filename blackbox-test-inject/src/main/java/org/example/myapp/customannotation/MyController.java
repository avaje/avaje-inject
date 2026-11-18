package org.example.myapp.customannotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.avaje.inject.Prototype;

/** Custom annotation that registers beans with prototype scope. */
@Prototype
@Target({TYPE, METHOD})
@Retention(SOURCE)
public @interface MyController {
}
