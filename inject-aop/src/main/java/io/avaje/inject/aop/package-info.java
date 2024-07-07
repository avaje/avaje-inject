/**
 * Support for Aspect Oriented Programming.
 * <p>
 * Aspects allow us to define method interceptors and then apply to specific methods or all public methods of a class.
 * <p>
 * An example of an Aspect might be <em>@Retry</em> which will invoke the underlying method and
 * then retry if that invocation fails.
 */
package io.avaje.inject.aop;
