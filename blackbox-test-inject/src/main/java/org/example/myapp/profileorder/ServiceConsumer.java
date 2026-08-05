package org.example.myapp.profileorder;

import jakarta.inject.Singleton;

@Singleton
public class ServiceConsumer {

  private final MyService service;

  public ServiceConsumer(MyService service) {
    this.service = service;
  }

  public String serviceName() {
    return service.name();
  }
}
