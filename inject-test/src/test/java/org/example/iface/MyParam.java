package org.example.iface;


import jakarta.inject.Singleton;

@Singleton
public class MyParam<T> implements IfaceParam<T> {

  @Override
  public T param() {
    return null;
  }
}
