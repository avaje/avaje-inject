package io.avaje.inject.generator.models.valid.autofactory;

import io.avaje.inject.AutoFactory;
import jakarta.inject.Singleton;

@Singleton
public class MyCreator implements AutoFactory.Creator {

  @Override
  public <T> T create(Class<T> type) {
    return null;
  }
}
