package io.avaje.inject.generator.models.valid.assist.generic;

public interface RelicFactory {
  <T> Relic<T> forge(Class<T> type);
}
