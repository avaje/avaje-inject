package org.example.myapp.profileorder.lastditch;

import jakarta.inject.Singleton;

/**
 * The third circle, also descending from the one above it. Two beans that both implement Circle and
 * depend on Circle is what stalls the strict ordering rounds.
 */
@Singleton
public class Gluttony implements Circle {

  public Gluttony(Circle above) {}
}
