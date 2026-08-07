package org.example.myapp.profileorder.lastditch;

import jakarta.inject.Singleton;

/** The second circle, descending from the one above it. */
@Singleton
public class Lust implements Circle {

  public Lust(Circle above) {}
}
