package io.avaje.inject.generator.models.valid.autofactory;

import io.avaje.inject.AutoFactory;
import io.avaje.inject.Primary;
import jakarta.inject.Named;

@Primary
@Named("mine")
@AutoFactory(MyCreator.class)
public interface MyMapper {

  String find(long id);

  @AutoFactory(MyCreator.class)
  interface Inner {
    String inner();
  }
}
