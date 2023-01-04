package org.example.generic.repo;

import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class MapService2 implements MapService<Map<String, Model2>> {

  final MapRepo2 repo;

  public MapService2(MapRepo2 repo) {
    this.repo = repo;
  }

  @Override
  public Map<String, Model2> get() {
    return Map.of("", repo.get());
  }
}
