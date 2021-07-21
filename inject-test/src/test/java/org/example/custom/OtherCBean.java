package org.example.custom;

import org.example.MyCustomScope;

@MyCustomScope
public class OtherCBean {

  final CustomBean dependency;

  public OtherCBean(CustomBean dependency) {
    this.dependency = dependency;
  }

  public CustomBean dependency() {
    return dependency;
  }
}
