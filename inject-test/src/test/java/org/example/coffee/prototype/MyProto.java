package org.example.coffee.prototype;

import io.avaje.inject.Prototype;
import org.example.coffee.Pump;

@Prototype
public class MyProto {

  final Pump pump;

  public MyProto(Pump pump) {
    this.pump = pump;
  }

  public Pump pump() {
    return pump;
  }
}
