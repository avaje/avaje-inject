package org.example.coffee.qualifier;

import jakarta.inject.Singleton;

@Singleton
public class MegaStoreManager {

  final SomeStore green;
  final SomeStore blue;
  final SomeStore noQualifier;

  public MegaStoreManager(@Green SomeStore green, @Blue SomeStore blue, SomeStore noQualifier) {
    this.green = green;
    this.blue = blue;
    this.noQualifier = noQualifier;
  }

}
