package org.example.myapp.assist.generic;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Inject;
import org.example.myapp.assist.Somthin;

@AssistFactory(RelicFactory.class)
public class Relic<T> {

  private final Somthin somthin;
  private final Class<T> type;

  @Inject
  Relic(Somthin somthin, @Assisted Class<T> type) {
    this.somthin = somthin;
    this.type = type;
  }

  public Class<T> type() {
    return type;
  }

  public Somthin somthin() {
    return somthin;
  }
}
