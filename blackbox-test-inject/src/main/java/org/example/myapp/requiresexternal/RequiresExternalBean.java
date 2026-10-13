package org.example.myapp.requiresexternal;

import io.avaje.inject.RequiresBean;
import jakarta.inject.Singleton;

@Singleton
@RequiresBean(ExternalService.class)
public class RequiresExternalBean {

  private final ExternalService service;

  RequiresExternalBean(ExternalService service) {
    this.service = service;
  }

  public ExternalService service() {
    return service;
  }
}
