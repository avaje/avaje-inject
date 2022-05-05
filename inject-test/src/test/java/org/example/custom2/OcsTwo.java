package org.example.custom2;

import javax.inject.Named;

@Named("two")
@OciMarker
@OtherScope
public class OcsTwo implements OciRock {

  final OcsOne one;

  OcsTwo(OcsOne one) {
    this.one = one;
  }

  public String twoPlusOne() {
    return "two+" + one.one();
  }
}
