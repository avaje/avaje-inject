package org.example.coffee.priority;

import javax.inject.Singleton;

@Singleton
@CustomPriority(1)
public class COtheri implements OtherIface {

  @Override
  public String other() {
    return "c";
  }
}
