/**
 * Avaje Inject API - see {@link io.avaje.inject.BeanScope}.
 *
 * <pre>{@code
 *   // Annotate classes with @Singleton, @Factory etc
 *
 *   @Singleton
 *   public class CoffeeMaker { ... }
 *
 *   @Singleton
 *   public class Pump { ... }
 *
 *  --------------------------------------------------
 *
 *   // Create BeanScope to wire and access beans
 *
 *   BeanScope scope = BeanScope.builder()
 *     .build();
 *
 *   // use it
 *   CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
 *   coffeeMaker.makeIt();
 *
 *   // close it to fire preDestroy lifecycle methods
 *   scope.close();
 *
 * }</pre>
 */
@org.jspecify.annotations.NullMarked
package io.avaje.inject;
