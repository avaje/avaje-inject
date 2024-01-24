package org.example.coffee.qualifier;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StoreManagerWithFieldQualifier {

  @Inject
  @Blue
  SomeStore store;

  public String store() {
    return store.store();
  }
}
