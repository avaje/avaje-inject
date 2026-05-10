package io.avaje.inject;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a class as the application's main class and entrypoint.
 *
 * <p>When present, the annotation processor will:
 *
 * <ul>
 *   <li>Enable strict wiring, ensuring all dependencies are satisfied at compile time
 *   <li>Write an {@code avaje-main-class.txt} resource file for the avaje inject gradle plugin
 *   <li>Update the project's {@code pom.xml} to configure the {@code maven-jar-plugin} with this
 *       class as the main class in the JAR manifest (if a {@code pom.xml} is present)
 * </ul>
 *
 * <p>Only one class in a project should be annotated with {@code @MainClass}. A compile error will
 * be raised if multiple classes are annotated.
 *
 * <h3>Example</h3>
 *
 * <pre>{@code
 * @MainClass
 * public class MyApplication {
 *
 *   public static void main(String[] args) {
 *     // start the application
 *   }
 * }
 * }</pre>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface MainClass {

  /**
   * Enable {@link InjectModule#interweave} wiring mode, optimizing multi-module wiring beyond
   * regular strict wiring.
   */
  boolean interweave() default false;

  /**
   * Enable {@link InjectModule#strictWiring()} mode, enforcing compile timemulti-module wiring
   * checks
   */
  boolean strictWiring() default true;
}
