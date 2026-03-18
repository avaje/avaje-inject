package org.example.myapp.assist.generic;

public interface RelicFactory {
  <T> Relic<T> forge(Class<T> type);
}
