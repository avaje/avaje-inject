package org.example.coffee.qualifier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoreManagerWithFieldQualifier {

  @Inject
  @Blue
  SomeStore store;

  public String store() {
    return store.store();
  }
}
