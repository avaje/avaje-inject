package org.example.generic;

import io.avaje.inject.Component;

import java.util.Optional;

@Component
public class MzCrudService implements CRUDService<MzObj, Integer> {

  @Override
  public String iamCrud() {
    return "MzCrud";
  }

  @Override
  public Integer create(MzObj bean) {
    return 92;
  }

  @Override
  public Optional<MzObj> get(Integer id) {
    return Optional.of(new MzObj());
  }
}
