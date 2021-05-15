package io.avaje.inject;

import javax.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a request scoped bean.
 * <p>
 * Request scoped beans are only available via {@link RequestScope}.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {

}
