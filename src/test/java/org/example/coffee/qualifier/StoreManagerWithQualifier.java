package org.example.coffee.qualifier;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class StoreManagerWithQualifier {

  private final SomeStore store;

  public StoreManagerWithQualifier(@Named("Blue") @Blue SomeStore store) {
    this.store = store;
  }

  public String store() {
    return store.store();
  }
}
