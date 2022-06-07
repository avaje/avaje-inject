package org.example.generic;

import io.avaje.inject.Component;

@Component
class MyCreateService implements CreateService<MyObj, Integer> {

  @Override
  public Integer create(MyObj bean) {
    return 42;
  }
}
