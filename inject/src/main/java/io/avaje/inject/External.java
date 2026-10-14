package io.avaje.inject;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks dependencies as external beans not managed by the current module. Compile-time validation
 * will be disabled for types annotated.
 *
 * <p>When placed on a constructor or method this applies to all of its parameters.
 *
 * <p>Use {@code @External} for a mandatory dependency that this module can't see at compile-time.
 *
 * <p>This can also be used as a meta-annotation, where an annotation annotated with
 * {@code @External} has the same effect as {@code @External}.
 */
@Documented
@Retention(CLASS)
@Target({FIELD, PARAMETER, CONSTRUCTOR, METHOD, ANNOTATION_TYPE})
public @interface External {}
