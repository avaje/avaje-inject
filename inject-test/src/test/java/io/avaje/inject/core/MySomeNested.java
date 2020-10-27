package io.avaje.inject.core;

import javax.inject.Singleton;

@Singleton
public class MySomeNested implements Some.Nested {

  @Override
  public String doNested() {
    return "MySomeNested";
  }
}
