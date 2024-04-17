package io.avaje.inject.event;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies the event parameter of an asynchronous observer method. May be applied to a parameter of a method
 * of a bean class.
 *
 * <pre>{@code
 *
 *   public void afterLogin(@ObservesAsync LoggedInEvent event) { ... }
 *
 * }</pre>
 *
 * <p>An observer method is a non-abstract method of a managed bean class.
 *
 * <p>Each observer method must have exactly one event parameter, of the same type as the event type
 * it observes. Event qualifiers may be declared by annotating the event parameter with {@code
 * Named} or any qualifier annotations. When searching for observer methods for an event, the
 * container considers the type and qualifiers of the event parameter.
 *
 * <p>If the event parameter does not explicitly declare any qualifier, the observer method observes
 * events with no qualifier.
 *
 * <p>The event parameter type may contain a type variable or wildcard.
 *
 * <p>In addition to the event parameter, observer methods may declare additional parameters, which
 * may declare qualifiers. These additional parameters are beans that will be injected when an event
 * has occurred.
 *
 * <pre>{@code
 *
 *   public void afterLogin(@ObservesAsync LoggedInEvent event, @Manager User user, Logger log) { ... }
 *
 * }</pre>
 */
@Target(PARAMETER)
@Retention(SOURCE)
public @interface ObservesAsync {

  /** The priority of the observe method */
  int priority() default 1000;

}
