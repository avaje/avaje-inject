package org.example.iface;

import jakarta.inject.Singleton;

@Singleton
public class ConcreteExtend implements IfaceExtend {

  @Override
  public String hello() {
    return "hello";
  }
}
