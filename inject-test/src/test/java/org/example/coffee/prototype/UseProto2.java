package org.example.coffee.prototype;

import javax.inject.Singleton;

@Singleton
public class UseProto2 {

  final MyProto myProto;
  final OtherProto otherProto;

  public UseProto2(MyProto myProto, OtherProto otherProto) {
    this.myProto = myProto;
    this.otherProto = otherProto;
  }

  MyProto myProto() {
    return myProto;
  }

  public OtherProto otherProto() {
    return otherProto;
  }
}
