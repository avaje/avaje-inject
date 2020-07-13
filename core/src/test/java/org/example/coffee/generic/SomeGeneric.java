package org.example.coffee.generic;

/**
 * Generics interface that we want to use as a dependency.
 */
public interface SomeGeneric<D> {

  String process(D data);
}
