package io.kanuka;

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
 * This annotation is typically placed on a top level interface in the module.
 * </p>
 *
 * <pre>{@code
 *
 * package org.example.featuretoggle;
 *
 * import io.kanuka.ContextModule;
 *
 * @ContextModule(name = "feature-toggle")
 * public interface FeatureToggle {
 *
 *   boolean isEnabled(String key);
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
