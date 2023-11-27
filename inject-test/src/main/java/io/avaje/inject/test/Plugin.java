package io.avaje.inject.test;

import java.lang.reflect.Type;

import io.avaje.inject.BeanScope;

/**
 * Plugin that can inject into fields.
 * <p>
 * The avaje-jex-test provides a plugin that would start a http server
 * and inject a http client into the field for the tests to use.
 */
public interface Plugin {

  /**
   * Return true if this plugin should inject for this class.
   */
  boolean forType(Type type);

  /**
   * Create a plugin scope for either EACH or ALL (per test or per class)
   * <p>
   * The scope will be closed appropriately (afterEach or afterAll).
   */
  Scope createScope(BeanScope beanScope);

  /**
   * The plugin scope for the test.
   */
  interface Scope {

    /**
     * Create an instance of this type to inject into the test.
     * <p>
     * For example, avaje-jex-test will create a http client.
     */
    Object create(Type type);

    /**
     * Close resources that are in this scope - for example, http server.
     */
    void close();
  }
}
