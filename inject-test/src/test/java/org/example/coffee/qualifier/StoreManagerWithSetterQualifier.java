package org.example.coffee.qualifier;

import javax.inject.Inject;
import javax.inject.Singleton;

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
