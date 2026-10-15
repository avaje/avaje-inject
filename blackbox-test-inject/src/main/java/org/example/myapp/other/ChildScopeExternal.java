package org.example.myapp.other;

import io.avaje.inject.External;
import io.avaje.inject.Prototype;
import jakarta.inject.Singleton;

@Singleton
class ChildScopeLeaf {
}

class ChildScopeTenant {
}

@Prototype
class ChildScopeTenantTx {

  private final ChildScopeTenant tenant;

  ChildScopeTenantTx(@External ChildScopeTenant tenant) {
    this.tenant = tenant;
  }

  ChildScopeTenant tenant() {
    return tenant;
  }
}
