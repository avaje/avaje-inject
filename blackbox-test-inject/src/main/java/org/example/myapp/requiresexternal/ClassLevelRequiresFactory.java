package org.example.myapp.requiresexternal;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

@Factory
@RequiresBean(ClassLevelService.class)
public class ClassLevelRequiresFactory {

  public static class FromClassLevel {
    private final ClassLevelService service;

    FromClassLevel(ClassLevelService service) {
      this.service = service;
    }

    public ClassLevelService service() {
      return service;
    }
  }

  @Bean
  FromClassLevel fromClassLevel(ClassLevelService service) {
    return new FromClassLevel(service);
  }
}
