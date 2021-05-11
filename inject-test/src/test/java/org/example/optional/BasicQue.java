package org.example.optional;


class BasicQue implements Que {

  private final String name;

  BasicQue(String name) {
    this.name = name;
  }

  @Override
  public String push(String msg) {
    return msg+"|"+name;
  }

  @Override
  public String toString() {
    return name;
  }
}
