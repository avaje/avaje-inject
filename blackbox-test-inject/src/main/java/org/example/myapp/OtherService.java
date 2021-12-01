package org.example.myapp;

import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyAround;

@Singleton
public class OtherService {

  @MyAround
  public String other(String param0, int param1) {
    return "other " + param0 + " " + param1;
  }

  public void notAopWrapped() {
    System.out.println("just a normal method");
  }
}
