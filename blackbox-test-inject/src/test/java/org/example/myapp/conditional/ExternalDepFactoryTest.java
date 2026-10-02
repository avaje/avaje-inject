package org.example.myapp.conditional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class ExternalDepFactoryTest {

  @Test
  void externalDepMissing() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.getOptional(ExternalDepFactory.class)).isEmpty();
      assertThat(beanScope.getOptional(ExternalDepService.class)).isEmpty();
    }
  }

  @Test
  void externalDepSupplied() {
    var dep = new ExternalDep();
    try (BeanScope beanScope = BeanScope.builder().bean(ExternalDep.class, dep).build()) {
      assertThat(beanScope.getOptional(ExternalDepFactory.class)).isPresent();
      assertThat(beanScope.get(ExternalDepService.class).dep()).isSameAs(dep);
    }
  }

  @Test
  void metaExternalDepMissing() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.getOptional(MetaExternalDepService.class)).isEmpty();
    }
  }

  @Test
  void metaExternalDepSupplied() {
    var dep = new MetaExternalDep();
    try (BeanScope beanScope = BeanScope.builder().bean(MetaExternalDep.class, dep).build()) {
      assertThat(beanScope.get(MetaExternalDepService.class).dep()).isSameAs(dep);
    }
  }
}
