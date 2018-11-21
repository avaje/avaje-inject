package io.dinject;

/**
 * Used to explicitly name a bean context and optionally specify if it depends on other bean contexts.
 * <p>
 * If this annotation is not present then the name will be derived as the "top level package name"
 * e.g. "org.example.featuretoggle"
 * </p>
 *
 * <p>
 * Typically there is a single bean context per Jar (module). In that sense the name is the "module name" and
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
 * import io.dinject.ContextModule;
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
 * In the example below we have the "Job System" which depends on the common "Feature Toggle"
 * module. When wiring the Job system module we expect some beans to be provided by the feature toggle
 * module (jar).
 * </p>
 *
 * <pre>{@code
 *
 * package org.example.jobsystem;
 *
 * import io.dinject.ContextModule;
 *
 * @ContextModule(name = "job-system", dependsOn = {"feature-toggle"})
 * public interface JobSystem {
 *
 *   ...
 * }
 *
 * }</pre>
 */
public @interface ContextModule {

  /**
   * The name of the context.
   */
  String name() default "";

  /**
   * The list of dependencies this context depends on.
   */
  String[] dependsOn() default {};

}
