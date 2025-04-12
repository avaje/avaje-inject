package org.multi.crosscut;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.AvajeModule;

class BeanCrossTest {

  private static Stream<Arguments> allModulesDefined() {
    return Stream.of(
      Arguments.of(Named.of("In working order A", new AvajeModule[]{
        new ModAModule(),
        new ModCModule(),
        new ModBModule(),
        new CrossCutModule(),
        new ModDModule(),
        new ModEModule(),
        new ManyModule(),
      })),
      Arguments.of(Named.of("In alphabetical order", new AvajeModule[]{
        new CrossCutModule(),
        new ManyModule(),
        new ModAModule(),
        new ModBModule(),
        new ModCModule(),
        new ModDModule(),
        new ModEModule(),
      })),
      Arguments.of(Named.of("In reverse alphabetical order", new AvajeModule[]{
        new ModEModule(),
        new ModDModule(),
        new ModCModule(),
        new ModBModule(),
        new ModAModule(),
        new ManyModule(),
        new CrossCutModule(),
      })),
      Arguments.of(Named.of("In shuffled order", new AvajeModule[]{
        new ModEModule(),
        new ModBModule(),
        new ModDModule(),
        new ManyModule(),
        new ModCModule(),
        new ModAModule(),
        new CrossCutModule(),
      }))
    );
  }

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

  @ParameterizedTest(name = "Multi Scope Test: {1}")
  @MethodSource("allModulesDefined")
  void bootstrap(AvajeModule... modules) {

    try (BeanScope beanScope = assertDoesNotThrow(() -> BeanScope.builder().modules(modules).build())) {
      for (final Class<?> clazz : CHECKABLE) {
        assertTrue(beanScope.getOptional(clazz).isPresent(), "Bean not found: " + clazz.getSimpleName());
      }
    }
  }
}
