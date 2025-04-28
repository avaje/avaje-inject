package org.example.myapp.lifecycle;

import io.avaje.inject.PostConstruct;

public abstract class ServiceCreator {
  @PostConstruct
  public void setup() {}
}
