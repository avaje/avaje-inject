package org.multi.parents;

import jakarta.inject.Singleton;
import org.multi.scope.Mod1Scope;
import org.multi.scope.Mod2Scope;

@Singleton
@Mod2Scope
public final class BeanIn2 {
  private final BeanIn1 beanIn1;
  public BeanIn2(final BeanIn1 beanIn1) {
    this.beanIn1 = beanIn1;
  }

  public BeanIn1 getBeanIn1() {
    return beanIn1;
  }
}
