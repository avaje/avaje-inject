package org.example.generic.repo;

import jakarta.inject.Singleton;

@Singleton
public class MapRepo1 extends AbstractRepo<Model1> {

  @Override
  public Model1 get() {
    return new Model1();
  }
}
