package org.example.coffee.prototype;

import io.avaje.inject.Prototype;

@Prototype
public class OtherProto {

  final MyProto myProto;

  public OtherProto(MyProto myProto) {
    this.myProto = myProto;
  }
}
