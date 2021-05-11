package org.example.circular;

import javax.inject.Inject;
import javax.inject.Singleton;

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
