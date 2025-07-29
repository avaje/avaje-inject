package org.multi.parents;

import jakarta.inject.Singleton;
import org.multi.scope.Mod3Scope;

@Singleton
@Mod3Scope
public final class BeanIn3 {
  private final BeanIn1 beanIn1;
  public BeanIn3(final BeanIn1 beanIn1) {
    this.beanIn1 = beanIn1;
  }
  public BeanIn1 getBeanIn1() {
    return beanIn1;
  }

}
