package org.example.iface;

import javax.inject.Singleton;

@Singleton
public class ConcreteExtend implements IfaceExtend {

  @Override
  public String hello() {
    return "hello";
  }
}
