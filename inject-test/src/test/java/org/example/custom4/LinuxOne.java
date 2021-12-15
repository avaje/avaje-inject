package org.example.custom4;

@LinuxScope
public class LinuxOne {

  private final Machine machine;
  private final Build build;

  LinuxOne(Machine machine, Build build) {
    this.machine = machine;
    this.build = build;
  }
}
