package org.example.myapp.conditional;

import jakarta.inject.Singleton;

@Singleton
@RequiresBird
public class BirdWatcher implements Watcher {

  private final Bird bird;

  BirdWatcher(Bird bird) {
    this.bird = bird;
  }

  @Override
  public void watch() {
    System.out.println("watching " + bird);
  }
}
