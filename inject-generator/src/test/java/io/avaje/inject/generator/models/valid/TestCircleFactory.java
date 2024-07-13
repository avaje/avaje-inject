package io.avaje.inject.generator.models.valid;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class TestCircleFactory {

  private final String parent;

  @Inject
  public TestCircleFactory(@Named("parent") String parent) {
    this.parent = parent;
  }

  public String getParent() {
    return parent;
  }
}
