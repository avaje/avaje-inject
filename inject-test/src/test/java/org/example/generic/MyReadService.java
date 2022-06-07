package org.example.generic;

import io.avaje.inject.Component;

import java.util.Optional;

@Component
public class MyReadService implements ReadService<MyObj, Integer> {

  @Override
  public Optional<MyObj> get(Integer id) {
    return Optional.empty();
  }
}
