package org.example.coffee.qualifier;

import org.example.coffee.qualifier.ColorStore.Color;

import jakarta.inject.Singleton;

@Singleton
public class StoreManagerWithQualifier {

  private final SomeStore store;

  public StoreManagerWithQualifier(@ColorStore(Color.BLUE) SomeStore store) {
    this.store = store;
  }

  public String store() {
    return store.store();
  }
}
