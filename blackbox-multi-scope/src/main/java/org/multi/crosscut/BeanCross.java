package org.multi.crosscut;

import org.multi.moda.BeanInModA;
import org.multi.modb.BeanInModB;
import org.multi.scope.CrossCutScope;

@CrossCutScope
public class BeanCross{
  public BeanCross(final BeanInModA beanInModA, final BeanInModB beanInModB) {

  }
}
