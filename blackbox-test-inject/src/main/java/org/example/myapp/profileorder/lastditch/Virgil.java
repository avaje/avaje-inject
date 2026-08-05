package org.example.myapp.profileorder.lastditch;

import io.avaje.inject.Profile;
import jakarta.inject.Singleton;

@Singleton
@Profile(none = "paradiso")
public class Virgil implements Guide {

  public Virgil(Circle circle) {}

  @Override
  public String name() {
    return "virgil";
  }
}
