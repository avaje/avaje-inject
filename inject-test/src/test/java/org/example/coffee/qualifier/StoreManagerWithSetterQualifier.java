package org.example.coffee.qualifier;

import org.example.coffee.qualifier.ColorStore.Color;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StoreManagerWithSetterQualifier {

  private final SomeStore blueStore;
  private SomeStore greenStore;

  public StoreManagerWithSetterQualifier(@ColorStore(Color.BLUE) SomeStore blueStore) {
    this.blueStore = blueStore;
  }

  @Inject
  public void setGreenStore(@Green SomeStore greenStore) {
    this.greenStore = greenStore;
  }

  public String blueStore() {
    return blueStore.store();
  }

  public String greenStore() {
    return greenStore.store();
  }
}
