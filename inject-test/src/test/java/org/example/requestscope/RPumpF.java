package org.example.requestscope;

class RPumpF implements RPump {

  final String data;

  RPumpF(String data) {
    this.data = data;
  }

  @Override
  public String pump() {
    return data;
  }
}
