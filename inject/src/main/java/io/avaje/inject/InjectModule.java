package io.avaje.inject;

/**
 * Used to explicitly specify if it depends on externally provided beans or provides.
 *
 * <h3>External dependencies</h3>
 * <p>
 * Use {@code requires} to specify dependencies that will be provided externally.
 *
 * <pre>{@code
 *
 *   // tell the annotation processor Pump and Grinder are provided externally
 *   // otherwise it will think we have missing dependencies at compile time
 *
 *   @InjectModule(requires = {Pump.class, Grinder.class})
 *
 * }</pre>
 *
 * <h3>Custom scope depending on another scope</h3>
 * <p>
 * When using custom scopes we can have the case where we desire one scope to depend
 * on another. In this case we put the custom scope annotation in requires.
 * <p>
 * For example lets say we have a custom scope called {@code StoreComponent} and that
 * depends on {@code QueueComponent} custom scope.
 *
 * <pre>{@code
 *
 *   @Scope
 *   @InjectModule(requires = {QueueComponent.class})
 *   public @interface StoreComponent {
 *   }
 *
 *
 * }</pre>
 */
public @interface InjectModule {

  /**
   * Explicitly specify the name of the module.
   */
  String name() default "";

  /**
   * Explicitly define features that are provided by this module and required by other modules.
   * <p>
   * This is used to order wiring across multiple modules. Modules that provide dependencies
   * should be wired before modules that require dependencies.
   */
  Class<?>[] provides() default {};

  /**
   * The dependencies that are provided externally or by other modules and that are required
   * when wiring this module.
   * <p>
   * This effectively tells the annotation processor that these types are expected to be
   * provided and to not treat them as missing dependencies. If we don't do this the annotation
   * processor thinks the dependency is missing and will error the compilation saying there is
   * a missing dependency.
   */
  Class<?>[] requires() default {};

  /**
   * Internal use only - identifies the custom scope annotation associated to this module.
   * <p>
   * When a module is generated for a custom scope this is set to link the module back to the
   * custom scope annotation and support partial compilation.
   */
  String customScopeType() default "";

}
