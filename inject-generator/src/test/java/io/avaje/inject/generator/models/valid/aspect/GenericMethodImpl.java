package io.avaje.inject.generator.models.valid.aspect;

import io.avaje.inject.Component;
import io.avaje.inject.generator.models.valid.Timed;

@Component
public class GenericMethodImpl implements GenericMethod {

  @Override
  @Timed
  public <T> T doIt(GenericMethodHolder<T> holder) {
    return holder.value;
  }
}
