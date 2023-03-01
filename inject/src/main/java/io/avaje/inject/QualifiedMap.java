package io.avaje.inject;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
 *   CrewMate(@QualifiedMap Map&lt;String, Tasks&gt taskMap) {
 *     this.taskMap = taskMap;
 *   }
 *
 * }
 * }</pre>
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface QualifiedMap {}
