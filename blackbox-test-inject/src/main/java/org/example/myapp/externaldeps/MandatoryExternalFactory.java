package org.example.myapp.externaldeps;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;

/**
 * {@code @External} on a bean method without any {@code @RequiresBean}. Only active when the
 * property is set such that the mandatory dependency doesn't impact other tests.
 */
@Factory
@RequiresProperty(value = "externaldeps.factory", equalTo = "on")
public class MandatoryExternalFactory {

  public static class UsesExternal {
    private final MandatoryExternal external;

    UsesExternal(MandatoryExternal external) {
      this.external = external;
    }

    public MandatoryExternal external() {
      return external;
    }
  }

  @Bean
  @External
  UsesExternal usesExternal(MandatoryExternal external) {
    return new UsesExternal(external);
  }
}
