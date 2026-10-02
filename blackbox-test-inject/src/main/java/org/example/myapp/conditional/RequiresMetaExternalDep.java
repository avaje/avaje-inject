package org.example.myapp.conditional;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

import io.avaje.inject.External;
import io.avaje.inject.RequiresBean;

/** Meta-annotation with the same effect as {@code @External} and {@code @RequiresBean}. */
@External
@RequiresBean(MetaExternalDep.class)
@Target(METHOD)
public @interface RequiresMetaExternalDep {
}
