package org.example.custom3;

import jakarta.inject.Singleton;

@MyThreeScope
@Singleton
public class TcsArt {

  final TcsBart bart;

  public TcsArt(TcsBart bart) {
    this.bart = bart;
  }
}
