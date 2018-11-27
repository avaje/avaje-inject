package org.example.coffee.qualifier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class StoreManagerWithFieldQualifier {

  @Named("Blue")
  @Inject
  @Blue
  SomeStore store;

  public String store() {
    return store.store();
  }
}
