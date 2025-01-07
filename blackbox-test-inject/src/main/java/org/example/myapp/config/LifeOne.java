package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class LifeOne {

  public String _state;

  @PostConstruct
  void post(BeanScope scope) {
    _state = "post|"
      + (scope != null ? "scope" : "");
  }
}
