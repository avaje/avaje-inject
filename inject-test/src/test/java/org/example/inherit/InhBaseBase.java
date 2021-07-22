package org.example.inherit;

import javax.inject.Inject;
import org.example.coffee.core.Steamer;

public class InhBaseBase implements InhBaseIface {

  @Inject
  Steamer expectSetBaseBaseField;

  private boolean expectFalseBaseBaseOverride;
  private boolean expectFalseBaseMethodSetOnBaseBase;
  private boolean expectTrueBaseBaseNoOverrideCalled;

  @Inject
  void baseBaseNoOverride(Steamer steamer) {
    expectTrueBaseBaseNoOverrideCalled = true;
  }

  @Inject
  void baseBaseOverride(Steamer steamer) {
    // overridden and no call to super (so not called)
    expectFalseBaseBaseOverride = true;
  }

  @Inject
  void baseMethod(Steamer steamer) {
    // overridden with no call to super (so not called)
    expectFalseBaseMethodSetOnBaseBase = true;
  }

  public Steamer expectSetBaseBaseField() {
    return expectSetBaseBaseField;
  }

  public boolean expectFalseBaseBaseOverride() {
    return expectFalseBaseBaseOverride;
  }

  public boolean expectFalseBaseMethodSetOnBaseBase() {
    return expectFalseBaseMethodSetOnBaseBase;
  }

  public boolean expectTrueBaseBaseNoOverrideCalled() {
    return expectTrueBaseBaseNoOverrideCalled;
  }
}
