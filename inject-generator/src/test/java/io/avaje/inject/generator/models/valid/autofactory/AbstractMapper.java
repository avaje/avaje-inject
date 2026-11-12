package io.avaje.inject.generator.models.valid.autofactory;

import io.avaje.inject.AutoFactory;

@AutoFactory(MyCreator.class)
public abstract class AbstractMapper {

  public abstract String find(long id);
}
