package org.example.missing;

import javax.inject.Singleton;

@Singleton
public class MFooUserMore {

  private final MFooUser fooUser;

  public MFooUserMore(MFooUser fooUser) {
    this.fooUser = fooUser;
  }
}
