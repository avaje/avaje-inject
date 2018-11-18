package org.example.coffee.provider;

import javax.inject.Singleton;

@Singleton
public class ProvOther2 {

  private final AProv aProv;

  public ProvOther2(AProv aProv) {
    this.aProv = aProv;
  }

  public String other() {
    return aProv.a();
  }

  public AProv getaProv() {
    return aProv;
  }
}
