package io.avaje.inject;

/**
 * Used to explicitly name a bean scope and optionally specify if it depends on other bean scopes.
 * <p>
 * If this annotation is not present then the name will be derived as the "top level package name"
 * e.g. "org.example.featuretoggle"
 * </p>
 *
 * <p>
 * Typically there is a single bean scope per Jar (module). In that sense the name is the "module name" and
 * the dependsOn specifies the names of modules that this depends on (provide beans that are used to wire this module).
 * </p>
 *
 * <p>
 * This annotation is typically placed on a top level interface or package-info in the module.
 * </p>
 *
 * <pre>{@code
 *
 * package org.example.featuretoggle;
 *
 * import io.avaje.inject.ContextModule;
 *
 * @ContextModule(name = "feature-toggle")
 * public interface FeatureToggle {
 *
 *   boolean isEnabled(String key);
 * }
 *
 * }</pre>
 *
 * <h2>dependsOn</h2>
 * <p>
 * We specify <code>dependsOn</code> when we have a module that depends on beans that
 * will be supplied by another module (jar).
 * </p>
 * <p>
 * In the example below we have the "Job System" which depends on the common "Feature Toggle" module.
 * When wiring the Job system module we expect some beans to be provided by the feature toggle module (jar).
 * </p>
 *
 * <pre>{@code
 *
 * package org.example.jobsystem;
 *
 * import io.avaje.inject.ContextModule;
 *
 * @ContextModule(name = "job-system", dependsOn = {"feature-toggle"})
 * public interface JobSystem {
 *
 *   ...
 * }
 *
 * }</pre>
 */
public @interface InjectModule {

  /**
   * The name of this context/module.
   * <p>
   * Other modules can then depend on this name and when they do they should wire after than module.
   * </p>
   */
  String name() default "";

  /**
   * Additional module features that is provided by this module.
   * <p>
   * These names are an addition to the module name and can be used in the <code>dependsOn</code> of other modules.
   * </p>
   *
   * <pre>{@code
   *
   * // A module that provides 'email-service' and also 'health-check'.
   * // ie. it has bean(s) that implement a health check interface
   * @ContextModule(name="email-service", provides={"health-checks"})
   *
   * // provides beans that implement a health check interface
   * // ... wires after 'email-service'
   * @ContextModule(name="main", provides={"health-checks"}, dependsOn={"email-service"})
   *
   * // wire this after all modules that provide 'health-checks'
   * @ContextModule(name="health-check-service", dependsOn={"health-checks"})
   *
   * }</pre>
   */
  String[] provides() default {};

  /**
   * The list of modules this context depends on.
   * <p>
   * Effectively dependsOn specifies the modules that must wire before this module.
   * </p>
   * <pre>{@code
   *
   * // wire after a module that is called 'email-service'
   * // ... or any module that provides 'email-service'
   *
   * @ContextModule(name="...", dependsOn={"email-service"})
   *
   * }</pre>
   */
  String[] dependsOn() default {};

}
