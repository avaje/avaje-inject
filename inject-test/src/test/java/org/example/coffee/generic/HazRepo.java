package org.example.coffee.generic;

import javax.inject.Singleton;

/**
 * Implementation of a generic interface.
 */
@Singleton
public class HazRepo implements Repository<Haz, Long> {

  @Override
  public Haz findById(Long id) {
    return new Haz(id);
  }
}
