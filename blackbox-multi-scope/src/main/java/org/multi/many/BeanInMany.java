package org.multi.many;

import org.multi.moda.BeanInModA;
import org.multi.modc.COther;
import org.multi.mode.BeanInModE;
import org.multi.scope.ManyScope;
import org.other.one.custom.OtherCustomComponent;

@ManyScope
public class BeanInMany {

  public BeanInMany(
      BeanInModE beanInModE, COther cOther, BeanInModA modA, OtherCustomComponent external) {}
}
