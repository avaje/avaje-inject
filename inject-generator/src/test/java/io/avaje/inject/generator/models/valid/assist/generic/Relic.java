package io.avaje.inject.generator.models.valid.assist.generic;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Inject;

@AssistFactory(RelicFactory.class)
public class Relic<T, T2> {

  private final Class<T> type;

  @Inject
  Relic(@Assisted Class<T> type, @Assisted Class<T2> type2) {
    this.type = type;
  }

  public Class<T> type() {
    return type;
  }
}
