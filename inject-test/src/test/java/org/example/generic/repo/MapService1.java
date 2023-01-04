package org.example.generic.repo;

import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class MapService1 implements MapService<Map<String, Model1>> {

  final MapRepo1 repo;

  public MapService1(MapRepo1 repo) {
    this.repo = repo;
  }

  @Override
  public Map<String, Model1> get() {
    return Map.of("", repo.get());
  }
}
