package org.multi.globalscope;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

/** Registers an exclusive JVM-global resource on construct, unregisters on destroy. */
@Singleton
public class RegistryBean {
  private final String id = "instance-" + System.identityHashCode(this);

  @PostConstruct
  void register() {
    GlobalRegistry.CONNECTIONS.put("conn", id);
  }

  @PreDestroy
  void unregister() {
    GlobalRegistry.CONNECTIONS.remove("conn");
  }

  public String lookup() {
    return GlobalRegistry.CONNECTIONS.get("conn");
  }
}
