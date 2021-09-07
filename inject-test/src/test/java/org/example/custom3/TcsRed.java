package org.example.custom3;

import org.example.custom2.OciMarker;
import org.example.custom2.OciRock;

@OciMarker
@MyThreeScope
public class TcsRed implements OciRock {

  final TcsArt art;

  public TcsRed(TcsArt art) {
    this.art = art;
  }
}
