package org.example.coffee.priority;

import javax.inject.Singleton;

@Singleton
@CustomPriority(7)
public class BOtheri implements OtherIface {

  @Override
  public String other() {
    return "b";
  }
}
