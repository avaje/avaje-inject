package io.avaje.inject.generator.models.valid.autofactory.pkg;

import jakarta.inject.Named;

@Named("pkg")
public interface PkgMapper {

  String find(long id);

  interface Nested {
    String nested();
  }
}
