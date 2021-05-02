package org.example.missing;

import jakarta.inject.Singleton;

@Singleton // improve error when @Singleton missing
public class MFoo {

  //@Inject
  //MFooUserMore more;

  public String mf() {
    return "mf";
  }
}
