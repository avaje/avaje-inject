package org.example.custom3;

import org.example.custom2.OcsOne;

@MyThreeScope
public class TcsA {

  final OcsOne one;
  final TcsRed red;

  /**
   * Other module supplied dependency is first (Issue #146)
   */
  public TcsA(OcsOne one, TcsRed red) {
    this.one = one;
    this.red = red;
  }
}
