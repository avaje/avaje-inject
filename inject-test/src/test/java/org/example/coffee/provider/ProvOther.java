package org.example.coffee.provider;

import javax.inject.Singleton;

@Singleton
public class ProvOther {

  private final AProv aProv;

  public ProvOther(AProv aProv) {
    this.aProv = aProv;
  }

  public String other() {
    return aProv.a();
  }

  public AProv getaProv() {
    return aProv;
  }
}
