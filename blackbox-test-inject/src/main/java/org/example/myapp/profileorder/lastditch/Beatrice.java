package org.example.myapp.profileorder.lastditch;

import io.avaje.inject.Profile;
import jakarta.inject.Singleton;

@Singleton
@Profile("paradiso")
public class Beatrice implements Guide {

  @Override
  public String name() {
    return "beatrice";
  }
}
