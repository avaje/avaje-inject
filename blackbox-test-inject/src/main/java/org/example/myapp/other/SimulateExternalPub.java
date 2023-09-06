package org.example.myapp.other;

public class SimulateExternalPub {

  private final SimulateExternalPub2 external2;

  public SimulateExternalPub(SimulateExternalPub2 external2) {
    this.external2 = external2;
  }

  public String doStuff() {
    return "asd Ex1|" + external2.doStuff();
  }
}
