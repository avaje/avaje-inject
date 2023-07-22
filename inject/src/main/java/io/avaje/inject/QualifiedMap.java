package io.avaje.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks an <code>Map&lt;String, T&gt; </code> field/parameter to receive a map of beans keyed
 * by qualifier name.
 *
 * <pre>{@code
 * class CrewMate {
 *
 *   private final Map<String, Tasks> taskMap;
 *
 *   @Inject
 *   CrewMate(@QualifiedMap Map<String, Tasks> taskMap) {
 *     this.taskMap = taskMap;
 *   }
 *
 * }
 * }</pre>
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface QualifiedMap {
}
