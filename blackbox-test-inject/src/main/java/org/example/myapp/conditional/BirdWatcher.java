package org.example.myapp.conditional;

import io.avaje.inject.RequiresBean;
import jakarta.inject.Singleton;

import java.awt.*;
import java.util.Properties;

@Singleton
@RequiresBean(missing = Properties.class)
@RequiresBean(missing = SystemColor.class)
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
