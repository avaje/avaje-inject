package org.example.collectionorder.api;

public class SchemaConfig {

  public final Mapper mapper;

  public SchemaConfig(Mapper mapper) {
    if (mapper == null) {
      throw new IllegalStateException("mapper is null");
    }
    this.mapper = mapper;
  }
}
