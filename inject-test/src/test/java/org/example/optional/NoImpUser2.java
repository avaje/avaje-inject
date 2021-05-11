package org.example.optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.Nullable;

@Singleton
class NoImpUser2 {

  @Inject @Nullable
  NoImpHere viaField;

  private NoImpHere viaMethod;

  @Inject
  void with(@Nullable NoImpHere viaMethod) {
    this.viaMethod = viaMethod;
  }

  boolean hasNoImplementationViaField() {
    return viaField == null;
  }

  boolean hasNoImplementationViaMethod() {
    return viaMethod == null;
  }

}
