package org.example.myapp.requiresexternal;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

@Factory
public class RequiresExternalFactory {

  public static class FromFactory {
    private final ExternalService service;

    FromFactory(ExternalService service) {
      this.service = service;
    }

    public ExternalService service() {
      return service;
    }
  }

  @Bean
  @RequiresBean(ExternalService.class)
  FromFactory fromFactory(ExternalService service) {
    return new FromFactory(service);
  }
}
