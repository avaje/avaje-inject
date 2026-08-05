package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class MetaDataOrderingTest {

  /**
   * Build a providers map from the given beans, mirroring the constructor logic in MetaDataOrdering.
   */
  private static Map<String, MetaDataOrdering.ProviderList> buildProviders(MetaData... beans) {
    var providers = new HashMap<String, MetaDataOrdering.ProviderList>();
    for (MetaData bean : beans) {
      providerAdd(providers, bean.toString()).add(bean);
      providerAdd(providers, bean.type()).add(bean);
      for (String provide : bean.provides()) {
        providerAdd(providers, provide).add(bean);
      }
    }
    return providers;
  }

  private static MetaDataOrdering.ProviderList providerAdd(Map<String, MetaDataOrdering.ProviderList> providers, String key) {
    return providers.computeIfAbsent(key.replace(", ", ","), s -> new MetaDataOrdering.ProviderList());
  }

  /**
   * A bean that depends on a type it also provides (e.g. a connection pool that takes a connection
   * factory and is itself a connection factory) must not block ordering. Prior to excluding the
   * bean itself from the provider check it could only be wired via the last ditch "any wired"
   * round, which allowed consumers of an interface to be ordered before other beans providing that
   * interface. See https://github.com/avaje/avaje-inject/issues/1049
   */
  @Test
  void ordering_beanProvidingTypeItDependsOn() {
    var conn = new MetaData("my.Conn", null);

    var pool = new MetaData("my.Pool", null);
    pool.setProvides(List.of("my.Conn"));
    pool.setDependsOn(List.of("my.Conn"));

    var dbService = new MetaData("my.DbService", null);
    dbService.setProvides(List.of("my.MyService"));
    dbService.setDependsOn(List.of("my.Pool"));

    var fileService = new MetaData("my.FileService", null);
    fileService.setProvides(List.of("my.MyService"));

    var consumer = new MetaData("my.Consumer", null);
    consumer.setDependsOn(List.of("my.MyService"));

    // consumer declared before dbService - the ordering must still delay it
    var beans = List.of(conn, pool, fileService, consumer, dbService);
    var ordering = new MetaDataOrdering(beans, new ScopeInfo());

    assertThat(ordering.processQueue()).isZero();

    var ordered = ordering.ordered();
    assertThat(ordered.indexOf(conn)).isLessThan(ordered.indexOf(pool));
    assertThat(ordered.indexOf(pool)).isLessThan(ordered.indexOf(dbService));
    // every bean providing my.MyService is ordered before the consumer of my.MyService
    assertThat(ordered.indexOf(dbService)).isLessThan(ordered.indexOf(consumer));
    assertThat(ordered.indexOf(fileService)).isLessThan(ordered.indexOf(consumer));
  }

  @Test
  void detectCycles_simple2NodeCycle() {
    // A depends on B, B depends on A
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setDependsOn(List.of("my.A"));

    var providers = buildProviders(a, b);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b), providers);

    assertThat(cycles).hasSize(1);
    assertThat(cycles.get(0)).containsExactly(a, b);
  }

  @Test
  void detectCycles_3NodeCycle() {
    // A -> B -> C -> A
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setDependsOn(List.of("my.C"));
    var c = new MetaData("my.C", null);
    c.setDependsOn(List.of("my.A"));

    var providers = buildProviders(a, b, c);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b, c), providers);

    assertThat(cycles).hasSize(1);
    assertThat(cycles.get(0)).containsExactly(a, b, c);
  }

  @Test
  void detectCycles_noCycle_linear() {
    // A -> B -> C (no cycle, linear chain)
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setDependsOn(List.of("my.C"));
    var c = new MetaData("my.C", null);
    // C has no dependencies

    var providers = buildProviders(a, b, c);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b, c), providers);

    assertThat(cycles).isEmpty();
  }

  @Test
  void detectCycles_noCycle_empty() {
    var cycles = MetaDataOrdering.detectCycles(List.of(), Map.of());
    assertThat(cycles).isEmpty();
  }

  @Test
  void detectCycles_multipleIndependentCycles() {
    // Cycle 1: A <-> B
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setDependsOn(List.of("my.A"));

    // Cycle 2: C <-> D
    var c = new MetaData("my.C", null);
    c.setDependsOn(List.of("my.D"));
    var d = new MetaData("my.D", null);
    d.setDependsOn(List.of("my.C"));

    var providers = buildProviders(a, b, c, d);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b, c, d), providers);

    assertThat(cycles).hasSize(2);
  }

  @Test
  void detectCycles_throughInterfaceProvides() {
    // A depends on "my.Iface", B provides "my.Iface" and depends on A
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.Iface"));

    var b = new MetaData("my.B", null);
    b.setProvides(List.of("my.Iface"));
    b.setDependsOn(List.of("my.A"));

    var providers = buildProviders(a, b);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b), providers);

    assertThat(cycles).hasSize(1);
    assertThat(cycles.get(0)).containsExactly(a, b);
  }

  @Test
  void detectCycles_noFalsePositive_depNotInRemainder() {
    // A depends on B, but B is not in the remainder (already wired)
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setWired();

    // B is in providers but NOT in the remainder list
    var providers = buildProviders(a, b);
    var cycles = MetaDataOrdering.detectCycles(List.of(a), providers);

    assertThat(cycles).isEmpty();
  }

  @Test
  void detectCycles_multiHopCycle() {
    // A -> B -> C -> D -> A (4-node cycle)
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.B"));
    var b = new MetaData("my.B", null);
    b.setDependsOn(List.of("my.C"));
    var c = new MetaData("my.C", null);
    c.setDependsOn(List.of("my.D"));
    var d = new MetaData("my.D", null);
    d.setDependsOn(List.of("my.A"));

    var providers = buildProviders(a, b, c, d);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b, c, d), providers);

    assertThat(cycles).hasSize(1);
    assertThat(cycles.get(0)).containsExactly(a, b, c, d);
  }

  @Test
  void detectCycles_cycleWithInterfaceMultiHop() {
    // A -> Iface (provided by B) -> C -> A
    var a = new MetaData("my.A", null);
    a.setDependsOn(List.of("my.Iface"));

    var b = new MetaData("my.B", null);
    b.setProvides(List.of("my.Iface"));
    b.setDependsOn(List.of("my.C"));

    var c = new MetaData("my.C", null);
    c.setDependsOn(List.of("my.A"));

    var providers = buildProviders(a, b, c);
    var cycles = MetaDataOrdering.detectCycles(List.of(a, b, c), providers);

    assertThat(cycles).hasSize(1);
    assertThat(cycles.get(0)).containsExactly(a, b, c);
  }
}
