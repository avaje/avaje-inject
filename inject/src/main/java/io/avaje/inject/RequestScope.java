package io.avaje.inject;

import jakarta.inject.Provider;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides request scoped beans in addition to beans from the underlying bean scope.
 */
public interface RequestScope extends Closeable {

  /**
   * Get a dependency.
   */
  <T> T get(Class<T> type);

  /**
   * Get a named dependency.
   */
  <T> T get(Class<T> type, String name);

  /**
   * Get an optional dependency.
   */
  <T> Optional<T> getOptional(Class<T> type);

  /**
   * Get an optional named dependency.
   */
  <T> Optional<T> getOptional(Class<T> type, String name);

  /**
   * Get an optional dependency potentially returning null.
   */
  <T> T getNullable(Class<T> type);

  /**
   * Get an optional named dependency potentially returning null.
   */
  <T> T getNullable(Class<T> type, String name);

  /**
   * Return Provider of T given the type.
   */
  <T> Provider<T> getProvider(Class<T> type);

  /**
   * Return Provider of T given the type and name.
   */
  <T> Provider<T> getProvider(Class<T> type, String name);

  /**
   * Get a list of dependencies for the interface type .
   */
  <T> List<T> list(Class<T> interfaceType);

  /**
   * Get a set of dependencies for the interface type .
   */
  <T> Set<T> set(Class<T> interfaceType);

  /**
   * Register a closable with the request scope.
   * <p>
   * All closable's registered here are closed at the end of the request scope.
   */
  void addClosable(Closeable closeable);

  /**
   * Close the scope firing any <code>@PreDestroy</code> methods.
   */
  void close();

}
