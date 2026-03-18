package io.avaje.inject.generator.models.valid.assist.generic;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Inject;

@AssistFactory(RelicFactory.class)
public class Relic<T> {

  private final Class<T> type;

  @Inject
  Relic(@Assisted Class<T> type) {
    this.type = type;
  }

  public Class<T> type() {
    return type;
  }
}
