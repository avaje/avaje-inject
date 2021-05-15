package io.avaje.inject;

import java.util.List;

/**
 * Match for request scope provider.
 */
public interface RequestScopeMatch<T> {

  /**
   * Return all the keys that match the provider.
   */
  List<String> keys();

  /**
   * Return the provider.
   */
  RequestScopeProvider<T> provider();
}
