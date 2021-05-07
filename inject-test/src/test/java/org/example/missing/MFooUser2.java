package org.example.missing;

import javax.inject.Singleton;

/**
 * Has only 1 public constructor so that is chosen for injection.
 */
@Singleton
public class MFooUser2 {

  private final MFoo mf;
  private final boolean usePublicConstructor;

  public MFooUser2(MFoo mf) {
    this.mf = mf;
    this.usePublicConstructor = true;
  }

  /**
   * Extra protected constructor usually for unit testing purposes only.
   */
  MFooUser2(MFoo mf, boolean dummy) {
    this.mf = mf;
    this.usePublicConstructor = true;
  }

  public boolean isUsePublicConstructor() {
    return usePublicConstructor;
  }
}
