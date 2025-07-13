package io.avaje.inject;

import java.util.Set;

/**
 * A bean entry with priority and optional name.
 *
 * @see BeanScope#all()
 */
public interface BeanEntry {

  /**
   * Priority of externally supplied bean.
   */
  int SUPPLIED = Integer.MIN_VALUE;

  /**
   * Priority of <code>@Primary</code> bean.
   */
  int PRIMARY = Integer.MIN_VALUE + 1;

  /**
   * Priority of normal bean.
   */
  int NORMAL = 0;

  /**
   * Priority of <code>@Secondary</code> bean.
   */
  int SECONDARY = Integer.MAX_VALUE;

  /**
   * Return the bean name.
   */
  String qualifierName();

  /**
   * Return the bean instance.
   */
  Object bean();

  /**
   * The bean instance type.
   */
  Class<?> type();

  /**
   * Return the wiring priority of the bean.
   */
  int priority();

  /**
   * Return the type keys for this bean.
   * <p>
   * This is the set of type, interface types and annotation types that the entry is registered for.
   */
  Set<String> keys();

  /**
   * Return true if the entry has a key for this type.
   * <p>
   * This is true if the keys contains the canonical name of the given type.
   *
   * @param type The type to match. Can be any type including concrete, interface or annotation type.
   */
  boolean hasKey(Class<?> type);

}
