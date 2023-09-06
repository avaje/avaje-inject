package org.example.myapp.other;

public class SimulateExternal {

  private final SimulateExternal2 external2;

  SimulateExternal(SimulateExternal2 external2) {
    this.external2 = external2;
  }

  public String doStuff() {
    return "asd Ex1|" + external2.doStuff();
  }
}
