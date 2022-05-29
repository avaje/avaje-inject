package org.example.factory;

import io.avaje.inject.Component;

@Component
public class MyUserOfOut {

  final OutSource source;

  public MyUserOfOut(OutSource source) {
    this.source = source;
  }
}
