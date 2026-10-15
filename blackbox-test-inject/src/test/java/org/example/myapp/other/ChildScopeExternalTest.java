package org.example.myapp.other;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class ChildScopeExternalTest {

  @Test
  void childScopeUsesSuppliedExternalDependency() {
    try (BeanScope rootScope = BeanScope.builder().build()) {
      assertThat(rootScope.get(ChildScopeLeaf.class)).isNotNull();

      var tenant = new ChildScopeTenant();
      try (BeanScope childScope = BeanScope.builder()
          .parent(rootScope, false)
          .bean("tenant", ChildScopeTenant.class, tenant) // with name
          .build()) {
        assertThat(childScope.get(ChildScopeTenantTx.class).tenant()).isSameAs(tenant);
      }
      try (BeanScope childScope = BeanScope.builder()
        .parent(rootScope, false)
        .bean(ChildScopeTenant.class, tenant) // type and instance
        .build()) {
        assertThat(childScope.get(ChildScopeTenantTx.class).tenant()).isSameAs(tenant);
      }
      try (BeanScope childScope = BeanScope.builder()
        .parent(rootScope, false)
        .beans(tenant) // instance
        .build()) {
        assertThat(childScope.get(ChildScopeTenantTx.class).tenant()).isSameAs(tenant);
      }
    }
  }
}
