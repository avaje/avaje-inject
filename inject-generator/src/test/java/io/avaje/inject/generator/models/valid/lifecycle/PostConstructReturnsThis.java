package io.avaje.inject.generator.models.valid.lifecycle;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class PostConstructReturnsThis {

  @PostConstruct
  public PostConstructReturnsThis init() {
    return this;
  }
}
