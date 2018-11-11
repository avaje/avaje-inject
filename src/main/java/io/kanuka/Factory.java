package io.kanuka;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A singleton bean that has methods marked with the <code>@Bean</code> annotation.
 * <p>
 * Factory beans allow us to build beans using logic in methods. These methods for example
 * often use environment variables and system properties into account when building the bean.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Factory {
}
