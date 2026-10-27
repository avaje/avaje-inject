package org.example.modules.client;

import jakarta.inject.Singleton;

/** The module under test, which does not need the Database. */
@Singleton
@ClientScope
public class CustomerClient {

  public String customer(int id) {
    return "customer-" + id;
  }
}
