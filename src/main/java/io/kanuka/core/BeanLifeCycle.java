package io.kanuka.core;

public interface BeanLifeCycle {

  void postConstruct();

  void preDestroy();
}
