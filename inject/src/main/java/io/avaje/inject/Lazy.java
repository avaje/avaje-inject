package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Singleton, Component or Factory method beans to be initialised lazily.
 * <p>
 * When annotating a {@link Factory} as {@code @Lazy} it means that the factory
 * itself is not lazy but all beans that it provides will have lazy initialisation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Lazy {
}
