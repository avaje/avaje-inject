package org.example.requestscope;

import io.avaje.inject.Request;
import jakarta.inject.Named;

//@Named("blues")
// "blues" is derived qualifier name based on short name relative to interface short name
@Request
public class BluesRStuff implements RStuff {

  private final RPump pump;

  public BluesRStuff(@Named("blue") RPump pump) {
    this.pump = pump;
  }

  String pump() {
    return pump.pump();
  }

  @Override
  public String stuff() {
    return "stuff_"+pump();
  }
}
