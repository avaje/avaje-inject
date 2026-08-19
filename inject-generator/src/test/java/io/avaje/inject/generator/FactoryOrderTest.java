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

  @Test
  void orderModules_softRequires_orderedAfterContributor() {
    // A injects List<Contrib> (a soft requires), B contributes a Contrib implementation.
    // A must be wired after B even though A is first in the classpath order.
    var moduleA = new ModuleData("mod.A", List.of("TypeA"), List.of(), List.of("Contrib"));
    var moduleB = new ModuleData("mod.B", List.of("Contrib"), List.of());

    var order = new FactoryOrder(List.of(moduleA, moduleB), Set.of()).orderModules();

    assertThat(order).containsExactly("mod.B", "mod.A");
  }

  @Test
  void orderModules_softRequires_noContributor_isIgnored() {
    var moduleA = new ModuleData("mod.A", List.of("TypeA"), List.of(), List.of("Contrib"));
    var moduleB = new ModuleData("mod.B", List.of("TypeB"), List.of());

    var order = new FactoryOrder(List.of(moduleA, moduleB), Set.of()).orderModules();

    assertThat(order).containsExactlyInAnyOrder("mod.A", "mod.B");
  }

  @Test
  void orderModules_softRequires_selfContributed_notBlocked() {
    // A both provides and consumes Contrib - the soft requires must not block A on itself
    var moduleA = new ModuleData("mod.A", List.of("Contrib"), List.of(), List.of("Contrib"));

    var order = new FactoryOrder(List.of(moduleA), Set.of()).orderModules();

    assertThat(order).containsExactly("mod.A");
  }

  @Test
  void orderModules_softRequires_cycle_doesNotStall() {
    // A consumes what B provides and vice versa - unsatisfiable as a strict ordering,
    // so the soft requirements are relaxed rather than dropping the modules
    var moduleA = new ModuleData("mod.A", List.of("TypeA"), List.of(), List.of("TypeB"));
    var moduleB = new ModuleData("mod.B", List.of("TypeB"), List.of(), List.of("TypeA"));

    var order = new FactoryOrder(List.of(moduleA, moduleB), Set.of()).orderModules();

    assertThat(order).containsExactlyInAnyOrder("mod.A", "mod.B");
  }

  @Test
  void orderModules_softRequires_afterHardRequires() {
    // C provides TypeC, B requires TypeC and contributes Contrib, A softly requires Contrib
    var moduleA = new ModuleData("mod.A", List.of(), List.of(), List.of("Contrib"));
    var moduleB = new ModuleData("mod.B", List.of("Contrib"), List.of("TypeC"));
    var moduleC = new ModuleData("mod.C", List.of("TypeC"), List.of());

    var order = new FactoryOrder(List.of(moduleA, moduleB, moduleC), Set.of()).orderModules();

    assertThat(order).containsExactly("mod.C", "mod.B", "mod.A");
  }
}
