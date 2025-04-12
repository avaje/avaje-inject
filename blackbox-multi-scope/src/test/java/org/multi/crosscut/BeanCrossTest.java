package org.multi.crosscut;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.multi.many.BeanInMany;
import org.multi.many.ManyModule;
import org.multi.moda.BeanInModA;
import org.multi.moda.ModAModule;
import org.multi.modb.BOther;
import org.multi.modb.BeanInModB;
import org.multi.modb.ModBModule;
import org.multi.modc.BeanInModC;
import org.multi.modc.COther;
import org.multi.modc.ModCModule;
import org.multi.modd.BeanInModD;
import org.multi.modd.ModDModule;
import org.multi.mode.BeanInModE;
import org.multi.mode.ModEModule;
import org.other.one.custom.ExternalModule;

import io.avaje.inject.BeanScope;

class BeanCrossTest {

  private static final Class<?>[] CHECKABLE = {
    BeanCross.class,
    BeanCross2.class,
    BeanCross3.class,
    BeanInMany.class,
    BeanInModA.class,
    BeanInModB.class,
    BOther.class,
    BeanInModC.class,
    COther.class,
    BeanInModD.class,
    BeanInModE.class
  };

  @Test
  void testCustomOrdering() {

    try (BeanScope beanScope =
        assertDoesNotThrow(
            () ->
                BeanScope.builder()
                    .modules(
                        new ModEModule(),
                        new ModBModule(),
                        new ModDModule(),
                        new ManyModule(),
                        new ModCModule(),
                        new ModAModule(),
                        new CrossCutModule(),
                        new ExternalModule())
                    .build())) {
      for (final Class<?> clazz : CHECKABLE) {
        assertTrue(
            beanScope.getOptional(clazz).isPresent(), "Bean not found: " + clazz.getSimpleName());
      }
    }
  }
}
