package org.example.circular;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class Seat {

  private final Cupholder cupholder;

  @Inject
  Seat(Cupholder cupholder) {
    this.cupholder = cupholder;
  }

  public Cupholder getCupholder() {
    return cupholder;
  }

  public String hello() {
    return "SeatHello";
  }
}
