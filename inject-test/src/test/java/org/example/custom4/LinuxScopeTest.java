package org.example.custom4;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinuxScopeTest {

  @Test
  void test_via_nested_scopes() {
    Build buildExternal = new Build();
    BuildModule buildModule = new BuildModule(buildExternal);

    // our top scope
    try (BeanScope buildScope = BeanScope.newBuilder()
      .withModules(buildModule)
      .build()) {

      Build build = buildScope.get(Build.class);
      assertThat(build).isSameAs(buildExternal);

      Machine machineExternal = new Machine();
      MachineModule machineModule = new MachineModule(machineExternal);

      // our middle scope (depends on top scope)
      try (BeanScope machineScope = BeanScope.newBuilder()
        .withParent(buildScope)
        .withModules(machineModule)
        .build()) {

        MachineOne machineOne = machineScope.get(MachineOne.class);
        assertThat(machineOne.build).isSameAs(buildExternal);
        assertThat(machineOne.machine).isSameAs(machineExternal);

        //  bottom scope depends on middle scope and transitively depends on top scope
        // this is our case for Issue 171 where LinuxOne depends on Build
        // which is transitively supplied via MachineScope
        try (BeanScope linuxScope = BeanScope.newBuilder()
          .withParent(machineScope)
          .withModules(new LinuxModule())
          .build()) {

          MachineOne machineOne2 = linuxScope.get(MachineOne.class);
          assertThat(machineOne2).isSameAs(machineOne);
          assertThat(machineOne2.build).isSameAs(buildExternal);
          assertThat(machineOne2.machine).isSameAs(machineExternal);

          LinuxOne linuxOne = linuxScope.get(LinuxOne.class);
          assertThat(linuxOne.build).isSameAs(buildExternal);
          assertThat(linuxOne.machine).isSameAs(machineExternal);
        }
      }
    }
  }


  @Test
  void test_via_flattened_module_structure() {
    // external dependencies
    Build buildExternal = new Build();
    Machine machineExternal = new Machine();

    // our 'flattened' bean scope
    try (BeanScope flatScope = BeanScope.newBuilder()
      // all our scope modules
      .withModules(new BuildModule(buildExternal), new MachineModule(machineExternal), new LinuxModule())
      .build()) {

      Build build = flatScope.get(Build.class);
      assertThat(build).isSameAs(buildExternal);

      MachineOne machineOne = flatScope.get(MachineOne.class);
      assertThat(machineOne.build).isSameAs(buildExternal);
      assertThat(machineOne.machine).isSameAs(machineExternal);

      LinuxOne linuxOne = flatScope.get(LinuxOne.class);
      assertThat(linuxOne.build).isSameAs(buildExternal);
      assertThat(linuxOne.machine).isSameAs(machineExternal);
    }
  }
}
