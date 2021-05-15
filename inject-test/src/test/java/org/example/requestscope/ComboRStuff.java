package org.example.requestscope;

import io.avaje.inject.Request;
import jakarta.inject.Named;

@Request
public class ComboRStuff {

  private final RStuff red;
  private final RStuff blue;

  public ComboRStuff(@Named("reds") RStuff red, @Named("blues") RStuff blue) {
    this.red = red;
    this.blue = blue;
  }


  String stuff() {
    return red.stuff() + " " + blue.stuff();
  }
}
