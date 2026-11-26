package org.example.myapp.autofactory;

import io.avaje.inject.AutoFactory;

@AutoFactory(MapperCreator.class)
public interface UserMapper {

  String findById(long id);
}
