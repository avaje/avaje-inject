package org.example.iface;


import javax.inject.Singleton;

@Singleton
public class MyParam<T> implements IfaceParam<T> {

  @Override
  public T param() {
    return null;
  }
}
