package org.example.custom3;

import io.avaje.inject.BeanScope;
import org.example.custom2.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParentScopeTest {

  @Test
  void parentScope() {

    final BeanScope parent = BeanScope.builder()
      .modules(new OtherModule())
      .build();

    final BeanScope scope = BeanScope.builder()
      .modules(new MyThreeModule())
      .parent(parent)
      .build();

    // factory for custom scope
    final TcsGreen green = scope.get(TcsGreen.class);
    assertThat(green).isNotNull();

    final TcsRed red = scope.get(TcsRed.class);
    final TcsBlue blue = scope.get(TcsBlue.class);
    final OcsOne ocsOne = scope.get(OcsOne.class);
    final OcsTwo ocsTwo = scope.get(OcsTwo.class);
    final OcsThree ocsThree = scope.get(OcsThree.class);

    // combined from both scopes
    final List<OciRock> allRocks = scope.list(OciRock.class);
    assertThat(allRocks).containsOnly(red, ocsOne, ocsTwo);

    final List<OciRock> allRocksAsType = scope.list((Type)OciRock.class);
    assertThat(allRocksAsType).containsOnly(red, ocsOne, ocsTwo);

    // combined from both scopes
    final List<Object> marked = scope.listByAnnotation(OciMarker.class);
    assertThat(marked).containsOnly(red, ocsTwo);

    // dependency wired from parent
    assertThat(blue).isNotNull();
    assertThat(blue.getDependency()).isSameAs(ocsThree);
  }

  @Test
  void module_classes() {
    MyThreeModule module = new MyThreeModule();
    Class<?>[] classes = module.classes();
    Set<Class<?>> asSet = new HashSet<>(Arrays.asList(classes));

    assertThat(asSet).contains(
      org.example.custom3.TcsFactory.class,
      org.example.custom3.TcsCart.class,
      org.example.custom3.TcsGreen.class,
      org.example.custom3.TcsBlue.class,
      org.example.custom3.TcsBart.class,
      org.example.custom3.TcsArt.class,
      org.example.custom3.TcsRed.class,
      org.example.custom3.TcsA.class);
  }
}
