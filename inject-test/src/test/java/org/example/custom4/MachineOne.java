package org.example.custom4;

@MachineScope
public class MachineOne {

  private final Build build;
  private final Machine machine;

  MachineOne(Build build, Machine machine) {
    this.build = build;
    this.machine = machine;
  }
}
