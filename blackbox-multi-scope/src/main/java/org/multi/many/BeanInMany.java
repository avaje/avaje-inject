package org.multi.many;

import org.multi.moda.BeanInModA;
import org.multi.modc.COther;
import org.multi.mode.BeanInModE;
import org.multi.scope.ManyScope;

@ManyScope
public class BeanInMany {

    private final BeanInModE beanInModE;
    private final COther cOther;
    private final BeanInModA modA;

    public BeanInMany(final BeanInModE beanInModE, final COther cOther, final BeanInModA modA) {
        this.beanInModE = beanInModE;
        this.cOther = cOther;
        this.modA = modA;
    }
}
