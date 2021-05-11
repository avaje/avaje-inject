package org.example.coffee.qualifier;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class StoreManagerWithNamed {

  private final SomeStore store;

  public StoreManagerWithNamed(@Named("Blue") SomeStore store) {
    this.store = store;
  }

  public String store() {
    return store.store();
  }
}
