package org.example.coffee.prototype;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class UseProto {

  final Provider<MyProto> myProto;
  final OtherProto otherProto;

  public UseProto(Provider<MyProto> myProto, OtherProto otherProto) {
    this.myProto = myProto;
    this.otherProto = otherProto;
  }

  MyProto myProto() {
    return myProto.get();
  }

  public OtherProto otherProto() {
    return otherProto;
  }
}
