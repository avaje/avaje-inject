/**
 * Avaje Inject API - see {@link io.avaje.inject.BeanScope}.
 * <p>
 * Create {@link io.avaje.inject.BeanScope} using a builder.
 * Obtain beans from the scope and use them.
 * <p>
 * We should ensure the BeanScope is closed in order to fire
 * preDestroy lifecycle methods. We can do this via a shutdown
 * hook, or try with resource block or explicitly via application code.
 *
 * <pre>{@code
 *
 *   BeanScope scope = BeanScope.newBuilder()
 *     .build();
 *
 *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
 *   coffeeMaker.makeIt();
 *
 *   // fire preDestroy lifecycle methods
 *   scope.close();
 *
 * }</pre>
 */
package io.avaje.inject;
