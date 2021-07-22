package org.example.inherit;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.example.coffee.core.Steamer;

@Singleton
public class InhOne extends InhBase {

  @Inject
  Steamer expectSetTopField;
  Steamer topMethod;
  private boolean expectTrueTopMethodCalled;

  @Inject
  void topMethod(Steamer steamer) {
    expectTrueTopMethodCalled = true;
    this.topMethod = steamer;
  }

  Steamer expectSetTopField() {
    return expectSetTopField;
  }

  Steamer getTopMethod() {
    return topMethod;
  }

  boolean expectTrueTopMethodCalled() {
    return expectTrueTopMethodCalled;
  }
}
