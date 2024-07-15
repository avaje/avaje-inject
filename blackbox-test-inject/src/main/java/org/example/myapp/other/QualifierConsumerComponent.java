package org.example.myapp.other;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
final class QualifierConsumerComponent {

  private final String parent;
  private final String child;

  @Inject
  QualifierConsumerComponent(@Named("parent") String parent, @Named("child") String child) {
    this.parent = parent;
    this.child = child;
  }

  String parent() {
    return parent;
  }

  String child() {
    return child;
  }
}
