package org.multi.parents;

import jakarta.inject.Singleton;
import org.multi.scope.Mod4Scope;

@Singleton
@Mod4Scope
public final class BeanIn4 {
  private final BeanIn1 beanIn1;
  private final BeanIn3 beanIn3;
  public BeanIn4(final BeanIn1 beanIn1, final BeanIn3 beanIn3) {
    this.beanIn1 = beanIn1;
    this.beanIn3 = beanIn3;
  }
  public BeanIn1 getBeanIn1() {
    return beanIn1;
  }
  public BeanIn3 getBeanIn3() {
    return beanIn3;
  }
}
