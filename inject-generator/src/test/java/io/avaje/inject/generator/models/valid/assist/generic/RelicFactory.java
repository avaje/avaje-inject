package io.avaje.inject.generator.models.valid.assist.generic;

public interface RelicFactory {
  <T, T2> Relic<T, T2> forge(Class<T> type, Class<T2> type2);
}
