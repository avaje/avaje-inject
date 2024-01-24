package org.example.coffee.qualifier;

import org.example.coffee.qualifier.ColorStore.Color;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StoreManagerWithFieldQualifier {

  @Inject
  @ColorStore(Color.BLUE)
  SomeStore store;

  public String store() {
    return store.store();
  }
}
