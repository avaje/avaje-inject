package org.example.missing;

import javax.inject.Singleton;

@Singleton // improve error when @Singleton missing
public class MFoo {

  //@Inject
  //MFooUserMore more;

  public String mf() {
    return "mf";
  }
}
