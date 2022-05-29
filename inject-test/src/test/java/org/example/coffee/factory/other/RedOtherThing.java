package org.example.coffee.factory.other;

import org.example.coffee.factory.Otherthing;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("red")
public class RedOtherThing implements Otherthing {

  @Override
  public String doOther() {
    return "red";
  }
}
