package org.example.myapp;

import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyTimed;

@Singleton
public class OtherService {

  @MyAround
  public String other(String param0, int param1) {
    return "other " + param0 + " " + param1;
  }

  @MyAround
  @MyTimed
  public void multi() {
    System.out.println("sdsd");
  }

  public void notAopWrapped() {
    System.out.println("just a normal method");
  }
}
