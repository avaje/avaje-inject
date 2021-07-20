package org.example.custom2;

@OciMarker
@OtherScope
public class OcsTwo implements OciRock {

  final OcsOne one;

  OcsTwo(OcsOne one) {
    this.one = one;
  }
}
