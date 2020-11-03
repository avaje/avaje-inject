package org.example.coffee.generic;

/**
 * Generics interface that we want to use as a dependency.
 */
public interface Repository<T, I> {

  T findById(I id);
}
