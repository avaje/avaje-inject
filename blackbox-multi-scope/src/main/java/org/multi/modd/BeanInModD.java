package org.multi.modd;

import org.multi.moda.BeanInModA;
import org.multi.scope.ModDScope;

@ModDScope
public class BeanInModD {

  private final BeanInModA beanA;

  public BeanInModD(final BeanInModA beanInModA) {
    this.beanA = beanInModA;
  }
}
