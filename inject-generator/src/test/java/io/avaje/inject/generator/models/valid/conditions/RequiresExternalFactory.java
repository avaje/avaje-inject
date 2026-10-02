package io.avaje.inject.generator.models.valid.conditions;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

@Factory
@RequiresBean(RequiredExternal.class)
public class RequiresExternalFactory {

  public static class FactoryBean {}

  public static class MethodBean {}

  @Bean
  FactoryBean factoryBean(RequiredExternal external) {
    return new FactoryBean();
  }

  @Bean
  @RequiresBean(RequiredExternal.class)
  MethodBean methodBean(RequiredExternal external) {
    return new MethodBean();
  }
}
