package org.example.myapp.interweave;

import org.other.one.interweave.BeanFromOther;
import org.other.one.interweave.BeanRequiresLocal;

import io.avaje.inject.Component;

/**
 * Local bean that depends on both external beans. Its presence confirms that the full
 * bidirectional dependency chain resolves correctly when both modules are interweaved.
 */
@Component
public class LocalConsumer {

  private final BeanFromOther fromOther;
  private final BeanRequiresLocal requiresLocal;

  public LocalConsumer(BeanFromOther fromOther, BeanRequiresLocal requiresLocal) {
    this.fromOther = fromOther;
    this.requiresLocal = requiresLocal;
  }

  public BeanFromOther fromOther() {
    return fromOther;
  }

  public BeanRequiresLocal requiresLocal() {
    return requiresLocal;
  }
}
