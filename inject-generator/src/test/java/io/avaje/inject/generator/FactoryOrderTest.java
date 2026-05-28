package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryOrderTest {

  @Test
  void orderModules_respectsDependencyChain() {
    // C provides TypeC (no requires)
    // B provides TypeB, requires TypeC
    // A requires TypeB
    // Loaded in wrong order (A, B, C) — correct order must be C, B, A
    var moduleC = new ModuleData("mod.C", List.of("TypeC"), List.of());
    var moduleB = new ModuleData("mod.B", List.of("TypeB"), List.of("TypeC"));
    var moduleA = new ModuleData("mod.A", List.of(), List.of("TypeB"));

    var order = new FactoryOrder(List.of(moduleA, moduleB, moduleC), Set.of()).orderModules();

    assertThat(order).containsExactly("mod.C", "mod.B", "mod.A");
  }

  @Test
  void orderModules_independentModulesPassThrough() {
    var moduleA = new ModuleData("mod.A", List.of("TypeA"), List.of());
    var moduleB = new ModuleData("mod.B", List.of("TypeB"), List.of());

    var order = new FactoryOrder(List.of(moduleA, moduleB), Set.of()).orderModules();

    assertThat(order).containsExactlyInAnyOrder("mod.A", "mod.B");
  }
}
