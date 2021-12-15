package org.example.custom4;

@MachineScope
public class MachineOne {

  final Build build;
  final Machine machine;

  MachineOne(Build build, Machine machine) {
    this.build = build;
    this.machine = machine;
  }
}
