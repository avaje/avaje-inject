package org.example.myapp.named;

import io.avaje.inject.QualifiedMap;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Set;

@Singleton
public class PayUser {

  private final Map<String, PayStore> stores;

  PayUser(@QualifiedMap Map<String, PayStore> stores) {
    this.stores = stores;
  }

  Set<String> keys() {
    return stores.keySet();
  }

  Set<Map.Entry<String, PayStore>> entries() {
    return stores.entrySet();
  }
}
