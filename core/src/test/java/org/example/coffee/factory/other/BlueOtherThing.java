package org.example.coffee.factory.other;

import org.example.coffee.factory.Otherthing;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("blue")
public class BlueOtherThing implements Otherthing {

  @Override
  public String doOther() {
    return "blue";
  }
}
