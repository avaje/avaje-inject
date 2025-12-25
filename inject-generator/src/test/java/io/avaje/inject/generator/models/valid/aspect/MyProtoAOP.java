package io.avaje.inject.generator.models.valid.aspect;

import io.avaje.inject.Prototype;
import io.avaje.inject.generator.models.valid.Timed;

@Prototype
public class MyProtoAOP {

  @Timed
  public void pump() {}
}
