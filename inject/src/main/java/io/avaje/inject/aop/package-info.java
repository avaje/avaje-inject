/**
 * Support for AOP Aspects.
 * <p>
 * Aspects allow us to define method interceptors and then have this method interception
 * apply to specific methods or all public methods of a class.
 * <p>
 * An example of an Aspect might be <em>Retry</em> which will invoke the underlying and
 * then retry potentially multiple times if that invocation fails.
 */
package io.avaje.inject.aop;
