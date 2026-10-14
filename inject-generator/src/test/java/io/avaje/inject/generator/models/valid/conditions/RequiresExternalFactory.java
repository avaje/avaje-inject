package io.avaje.inject.generator.models.valid.conditions;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

@Factory
@RequiresBean(RequiredExternal.class)
public class RequiresExternalFactory {

  public static class ExternalBean {}

  @Bean
  @External
  ExternalBean externalBean(RequiredExternal external) {
    return new ExternalBean();
  }
}
