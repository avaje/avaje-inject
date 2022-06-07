package org.example.generic;

import io.avaje.inject.Component;

import java.util.Optional;

@Component
public class MyCrudService implements CRUDService<MyObj, Integer> {

  final ReadService<MyObj, Integer> read;
  final CreateService<MyObj, Integer> create;

  public MyCrudService(ReadService<MyObj, Integer> read, CreateService<MyObj, Integer> create) {
    this.read = read;
    this.create = create;
  }

  @Override
  public String iamCrud() {
    return "MyCrud";
  }

  @Override
  public Integer create(MyObj bean) {
    return create.create(bean);
  }

  @Override
  public Optional<MyObj> get(Integer id) {
    return read.get(id);
  }
}
