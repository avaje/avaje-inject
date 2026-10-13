package org.example.myapp.requiresexternal;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.myapp.requiresexternal.ClassLevelRequiresFactory.FromClassLevel;
import org.example.myapp.requiresexternal.RequiresExternalFactory.FromFactory;
import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

/** {@code @RequiresBean} types that are also dependencies don't need to be provided in this module. */
class RequiresExternalTest {

  @Test
  void notProvided_beansNotWired() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.getOptional(RequiresExternalBean.class)).isEmpty();
      assertThat(scope.getOptional(FromFactory.class)).isEmpty();
    }
  }

  @Test
  void provided_beansWired() {
    ExternalService external = new ExternalService() {};
    try (BeanScope scope = BeanScope.builder().bean(ExternalService.class, external).build()) {
      assertThat(scope.get(RequiresExternalBean.class).service()).isSameAs(external);
      assertThat(scope.get(FromFactory.class).service()).isSameAs(external);
    }
  }

  @Test
  void classLevelRequiresBean_notProvided() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.getOptional(ClassLevelRequiresFactory.class)).isEmpty();
      assertThat(scope.getOptional(FromClassLevel.class)).isEmpty();
    }
  }

  @Test
  void classLevelRequiresBean_provided() {
    ClassLevelService external = new ClassLevelService() {};
    try (BeanScope scope = BeanScope.builder().bean(ClassLevelService.class, external).build()) {
      assertThat(scope.get(FromClassLevel.class).service()).isSameAs(external);
    }
  }
}
