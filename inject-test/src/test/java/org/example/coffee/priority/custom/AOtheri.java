package org.example.coffee.priority.custom;

import javax.inject.Singleton;

@Singleton
@CustomPriority(42)
public class AOtheri implements OtherIface {

  @Override
  public String other() {
    return "a";
  }
}
