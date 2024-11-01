package org.example.myapp.other;

import org.other.one.OtherComponent;

import jakarta.inject.Singleton;

@Singleton
public class WireOther {
  OtherComponent component;

  public WireOther(OtherComponent component) {
    this.component = component;
  }
}
