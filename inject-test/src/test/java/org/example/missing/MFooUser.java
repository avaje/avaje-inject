package org.example.missing;

import javax.inject.Singleton;

@Singleton
public class MFooUser {

  private final MFoo mf;
  private final boolean usePublicConstructor;

  MFooUser(MFoo mf) {
    this.mf = mf;
    this.usePublicConstructor = true;
  }

  private MFooUser(MFoo mf, boolean dummy) {
    this.mf = mf;
    this.usePublicConstructor = true;
  }

  public boolean isUsePublicConstructor() {
    return usePublicConstructor;
  }
}
