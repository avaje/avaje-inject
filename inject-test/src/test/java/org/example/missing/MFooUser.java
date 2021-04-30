package org.example.missing;

import javax.inject.Singleton;

@Singleton
public class MFooUser {

  private final MFoo mf;

  public MFooUser(MFoo mf) {
    this.mf = mf;
  }
}
