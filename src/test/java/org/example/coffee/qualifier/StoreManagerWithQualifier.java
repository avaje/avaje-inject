package org.example.coffee.qualifier;

import javax.inject.Singleton;

@Singleton
public class StoreManagerWithQualifier {

  private final SomeStore store;

  public StoreManagerWithQualifier(@Blue SomeStore store) {
    this.store = store;
  }

  public String store() {
    return store.store();
  }
}
