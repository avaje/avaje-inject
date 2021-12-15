package org.example.custom4;

@LinuxScope
public class LinuxOne {

  final Machine machine;
  final Build build;

  LinuxOne(Machine machine, Build build) {
    this.machine = machine;
    this.build = build;
  }
}
