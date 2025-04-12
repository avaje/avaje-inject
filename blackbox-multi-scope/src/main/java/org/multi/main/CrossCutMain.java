package org.multi.main;

import org.multi.crosscut.BeanCross;
import org.multi.crosscut.BeanCross2;
import org.multi.crosscut.BeanCross3;
import org.multi.crosscut.CrossCutModule;
import org.multi.moda.BeanInModA;
import org.multi.modb.BeanInModB;

import io.avaje.inject.BeanScope;

public class CrossCutMain {

  public static void main(String[] args) {
    try (BeanScope beanScope = buildScope()) {
      beanScope.get(BeanInModB.class);
      beanScope.get(BeanInModA.class);
      beanScope.get(BeanCross.class);
      beanScope.get(BeanCross2.class);
      beanScope.get(BeanCross3.class);
    }
  }

  public static BeanScope buildScope() {
    return BeanScope.builder().modules(CrossCutModule.allRequiredModules()).build();
  }
}
