package org.multi.crosscut;

import org.multi.moda.BeanInModA;
import org.multi.modc.BeanInModC;
import org.multi.scope.CrossCutScope;

@CrossCutScope
public class BeanCross2 {
  public BeanCross2(final BeanInModA beanInModA, final BeanInModC beanInModC) {}
}
