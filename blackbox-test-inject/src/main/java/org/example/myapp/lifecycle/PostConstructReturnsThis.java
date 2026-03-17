package org.example.myapp.lifecycle;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class PostConstructReturnsThis {

  @PostConstruct
  public PostConstructReturnsThis init() {
    return this;
  }
}
