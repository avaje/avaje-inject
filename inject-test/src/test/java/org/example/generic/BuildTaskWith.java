package org.example.generic;

public interface BuildTaskWith<T> extends BuildTask {

  void prepareWith(T param);
}
