package org.example.myapp.lazy2;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class LazyOneA {

  public static final AtomicBoolean AINIT = new AtomicBoolean();
  public static final AtomicBoolean A_POST_CONSTRUCT = new AtomicBoolean();

  LazyOneA() {
    AINIT.set(true);
  }

  @PostConstruct
  void postConstruct() {
    A_POST_CONSTRUCT.set(true);
  }

  public String oneA() {
    return "oneA";
  }
}
