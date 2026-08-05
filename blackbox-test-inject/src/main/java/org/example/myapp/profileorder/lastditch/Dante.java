package org.example.myapp.profileorder.lastditch;

import jakarta.inject.Singleton;

/** Needs a guide, and is declared before the only guide he can be given. */
@Singleton
public class Dante {

  private final Guide guide;

  public Dante(Guide guide) {
    this.guide = guide;
  }

  public String guideName() {
    return guide.name();
  }
}
