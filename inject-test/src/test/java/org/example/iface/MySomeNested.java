package org.example.iface;

import jakarta.inject.Singleton;

@Singleton
public class MySomeNested implements Some.Nested {

  @Override
  public String doNested() {
    return "MySomeNested";
  }
}
