package org.example.myapp.conditional;

import jakarta.inject.Singleton;

@Singleton
@RequiresBird
public class BirdWatcher {

  private final Bird bird;

  BirdWatcher(Bird bird) {
    this.bird = bird;
  }

  public void watch() {
    System.out.println("watching " + bird);
  }
}
