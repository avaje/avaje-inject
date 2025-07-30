package org.multi.parents;

import io.avaje.inject.RequiresBean;
import jakarta.inject.Singleton;
import org.multi.scope.Mod4Scope;

@Singleton
@Mod4Scope
// Generate `contains` method calls in `BeanIn4$DI` to test DBuilder checks parent scope
@RequiresBean({BeanIn1.class, BeanIn3.class})
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
