package org.example.coffee.qualifier;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StoreManagerWithSetterQualifier {

  private final SomeStore blueStore;
  private SomeStore greenStore;

  public StoreManagerWithSetterQualifier(@Blue SomeStore blueStore) {
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
