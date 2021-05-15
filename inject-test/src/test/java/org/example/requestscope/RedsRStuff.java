package org.example.requestscope;

import io.avaje.inject.Request;
import jakarta.inject.Named;

@Named("reds")
@Request
public class RedsRStuff implements RStuff {

  private final RPump pump;

  public RedsRStuff(@Named("red") RPump pump) {
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
