package org.example.custom3;

import io.avaje.inject.BeanScope;
import org.example.custom2.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParentScopeTest {

  @Test
  void parentScope() {

    final BeanScope parent = BeanScope.newBuilder()
      .withModules(new OtherModule())
      .build();

    final BeanScope scope = BeanScope.newBuilder()
      .withModules(new MyThreeModule())
      .withParent(parent)
      .build();

    final TcsRed red = scope.get(TcsRed.class);
    final TcsBlue blue = scope.get(TcsBlue.class);
    final OcsOne ocsOne = scope.get(OcsOne.class);
    final OcsTwo ocsTwo = scope.get(OcsTwo.class);
    final OcsThree ocsThree = scope.get(OcsThree.class);

    // combined from both scopes
    final List<OciRock> allRocks = scope.list(OciRock.class);
    assertThat(allRocks).containsOnly(red, ocsOne, ocsTwo);

    // combined from both scopes
    final List<Object> marked = scope.listByAnnotation(OciMarker.class);
    assertThat(marked).containsOnly(red, ocsTwo);

    // dependency wired from parent
    assertThat(blue.getDependency()).isSameAs(ocsThree);
  }
}
