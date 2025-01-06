package org.example.myapp.config;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class LifeThree {

  public String _state;

  @PostConstruct
  void post(LifeOne one) {
    _state = "post|"
      + (one != null ? "one" : "");
  }
}
