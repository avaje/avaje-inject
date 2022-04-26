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
   * Set to true to ignore anything annotated with <code>@Singleton</code>.
   * <p>
   * Set this to true when some other library is using <code>@Singleton</code> and we want
   * avaje-inject to be completely independent of that by ignoring the standard <code>@Singleton</code>.
   * <p>
   * We instead use <code>@Component</code> instead of <code>@Singleton</code>.
   */
  boolean ignoreSingleton() default false;

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
   * Dependencies in these packages are expected to be provided by other modules.
   * <p>
   * Instead of listing each and every dependency in {@code requires} we can use this to specify
   * that any required dependency that is in these packages is expected to be provided by another module.
   * <p>
   * Use this rather than {@code requires} when there are lots of required dependencies, and we don't
   * want to list each one in {@code requires} and {@code provides}.
   */
  Class<?>[] requiresPackages() default {};

  /**
   * Internal use only - identifies the custom scope annotation associated to this module.
   * <p>
   * When a module is generated for a custom scope this is set to link the module back to the
   * custom scope annotation and support partial compilation.
   */
  String customScopeType() default "";

}
