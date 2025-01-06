package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class LifeTwo {

  public String _state;

  @PostConstruct
  void post(LifeOne one, BeanScope scope) {
    _state = "post|"
      + (one != null ? "one|" : "")
      + (scope != null ? "scope" : "");
  }
}
