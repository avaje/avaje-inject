package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the ordering bug reported in
 * https://github.com/avaje/avaje-inject/issues/1049
 *
 * <p>Confirmed against the real-world reproducer at
 * https://github.com/daviian/avaja-inject-reproducer - the actual (non-synthetic) trigger there
 * is a "decorator" style bean: {@code io.r2dbc.pool.ConnectionPool implements
 * io.r2dbc.spi.ConnectionFactory} while ALSO having a constructor dependency on a
 * {@code ConnectionFactory} (the one it wraps). This means {@code ConnectionPool} is itself a
 * member of the "ConnectionFactory" provider list AND depends on that same provider list, so it
 * could never satisfy "all providers wired" under strict semantics (it would require itself to
 * already be wired). Note this is a perfectly valid, always-resolvable-at-runtime pattern - not a
 * real error.
 *
 * <p>Root cause: {@link MetaDataOrdering#processQueue()} resolves the wiring order in 3 phases,
 * the last being a "last ditch" phase that relaxes a multi-implementation dependency so that it
 * is considered satisfied once ANY ONE implementation is wired, rather than ALL implementations.
 * That relaxed ("anyWired") check used to be applied as a blanket flag to every still-queued bean
 * in that phase - not scoped to only the specific provider list that actually had a permanently-
 * unresolvable-under-strict-semantics member (the self-referencing ConnectionPool/ConnectionFactory
 * pair). So once the decorator bean forced the algorithm to reach the last-ditch phase at all, a
 * Consumer of a completely unrelated, otherwise-fully-resolvable multi-implementation interface
 * (MyService, satisfied by profile-conditional CloudService/LocalService) could get wired as soon
 * as ONE implementation (CloudService) was wired, even though the other implementation
 * (LocalService) had not been wired yet - producing the reported wrong order: CloudService,
 * Consumer, LocalService.
 *
 * <p>Fix, in two parts: {@link MetaDataOrdering.ProviderList#isAllWired} / {@code isAnyWired} now
 * exclude the bean being checked from its own "provides" requirement, so a decorator/wrapper bean
 * resolves correctly in the normal strict rounds without ever needing the blanket last-ditch
 * relaxation. Additionally the last-ditch round now orders the beans it makes eligible such that
 * providers come before their consumers, so any other bean that forces that round can no longer
 * leak leniency into the ordering of an unrelated multi-implementation interface.
 */
class Issue1049Test {

  @Test
  void consumerOrderedBeforeAllInterfaceImplementations_whenUnrelatedBeanForcesLastDitchRound() {
    // CloudService: no deps, provides MyService -- wired immediately (round 0 / noDepends)
    var cloud = new MetaData("my.CloudService", null);
    cloud.setProvides(List.of("my.MyService"));

    // GateA: no deps, provides Gate -- wired immediately
    var gateA = new MetaData("my.GateA", null);
    gateA.setProvides(List.of("my.Gate"));

    // GateB: depends on itself, so it can never be wired (permanently unresolved)
    var gateB = new MetaData("my.GateB", null);
    gateB.setProvides(List.of("my.Gate"));
    gateB.setDependsOn(List.of("my.GateB"));

    // LocalService: depends on "my.Gate" (satisfied only via ALL of [gateA, gateB] wired,
    // which never happens under strict semantics because gateB can never wire)
    var local = new MetaData("my.LocalService", null);
    local.setProvides(List.of("my.MyService"));
    local.setDependsOn(List.of("my.Gate"));

    // Consumer: depends on "my.MyService" (provided by both cloud and local)
    var consumer = new MetaData("my.Consumer", null);
    consumer.setDependsOn(List.of("my.MyService"));

    // Order beans so Consumer is queued/iterated BEFORE LocalService (matches real generator
    // encounter order in the reported issue - see build_Consumer before build_LocalService)
    var ordering = new MetaDataOrdering(List.of(cloud, gateA, gateB, consumer, local), new ScopeInfo());
    ordering.processQueue();

    var orderedTypes = ordering.ordered().stream().map(MetaData::type).collect(Collectors.toList());

    int consumerIdx = orderedTypes.indexOf("my.Consumer");
    int localIdx = orderedTypes.indexOf("my.LocalService");

    // Expected: LocalService (an implementation Consumer depends on) must be wired
    // BEFORE Consumer, regardless of unrelated beans elsewhere forcing a lenient last-ditch round.
    assertThat(localIdx).isLessThan(consumerIdx);
  }

  @Test
  void consumerOrderedBeforeAllInterfaceImplementations_realWorldDecoratorBeanTrigger() {
    // Config: no deps -- wired immediately (analogous to the @Factory class itself)
    var config = new MetaData("my.Config", null);

    // FooA: the plain ConnectionFactory bean (Config.a()) - depends only on Config, provides Foo
    var fooA = new MetaData("my.FooA", null);
    fooA.setProvides(List.of("my.Foo"));
    fooA.setDependsOn(List.of("my.Config"));

    // FooWrapper: analogous to ConnectionPool, which implements ConnectionFactory (Foo) AND
    // has a constructor dependency on a Foo (the one it wraps). It is therefore a member of
    // its own required provider list for "Foo", which could never be "all wired" under strict
    // semantics (it would require itself to already be wired).
    var fooWrapper = new MetaData("my.FooWrapper", null);
    fooWrapper.setProvides(List.of("my.Foo", "my.FooWrapper"));
    fooWrapper.setDependsOn(List.of("my.Config", "my.Foo"));

    // CloudService: no deps, provides MyService -- wired immediately
    var cloud = new MetaData("my.CloudService", null);
    cloud.setProvides(List.of("my.MyService"));

    // LocalService: depends on FooWrapper (analogous to DbService depending on ConnectionPool),
    // provides MyService
    var local = new MetaData("my.LocalService", null);
    local.setProvides(List.of("my.MyService"));
    local.setDependsOn(List.of("my.FooWrapper"));

    // Consumer: depends on MyService (provided by both cloud and local)
    var consumer = new MetaData("my.Consumer", null);
    consumer.setDependsOn(List.of("my.MyService"));

    // Matches the real generator's encounter order: Config, Consumer, LocalService, CloudService,
    // FooWrapper, FooA (roughly alphabetical local-class order, factory beans grouped near Config)
    var ordering = new MetaDataOrdering(
      List.of(config, consumer, local, cloud, fooWrapper, fooA), new ScopeInfo());
    ordering.processQueue();

    var orderedTypes = ordering.ordered().stream().map(MetaData::type).collect(Collectors.toList());

    int consumerIdx = orderedTypes.indexOf("my.Consumer");
    int localIdx = orderedTypes.indexOf("my.LocalService");

    // Expected: LocalService must be wired before Consumer, matching the real bug report order
    // build_reproducer_Consumer(builder) before build_reproducer_DbService(builder).
    assertThat(localIdx).isLessThan(consumerIdx);
  }

  @Test
  void directSelfDependency_stillDetectedAsCircular_notSilentlySatisfied() {
    // A bean depending on its own exact type with no other implementation/provider must NOT be
    // silently treated as "satisfied" by the self-exclusion fix - it should remain unwired so
    // the normal circular dependency detection/reporting still applies. (Unresolved beans are
    // still appended to ordered() at the end so generated code compiles, but never marked wired.)
    var selfDependent = new MetaData("my.SelfDependent", null);
    selfDependent.setDependsOn(List.of("my.SelfDependent"));

    var ordering = new MetaDataOrdering(List.of(selfDependent), new ScopeInfo());
    int remaining = ordering.processQueue();

    assertThat(remaining).isEqualTo(1);
    assertThat(selfDependent.isWired()).isFalse();
  }
}
