package org.example.myapp.injector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.other.one.injector.ImportedDependency;
import org.other.one.injector.ImportedTarget;

import io.avaje.inject.BeanScope;
import jakarta.inject.Inject;

class InstanceInjectionTest {

  @Test
  void injectsExistingInstanceAndHandlesLifecycle() {
    InjectedThing existingDependency = new InjectedThing();
    ExternalInstance instance = new ExternalInstance(existingDependency);

    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.inject(instance)).isSameAs(instance);
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.methodDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.dependencies).containsExactly(scope.get(InjectedThing.class));
      assertThat(instance.injectedScope).isSameAs(scope);
      assertThat(instance.initialized).isTrue();
      assertThat(instance.initializedScope).isSameAs(scope);
      assertThat(instance.initializeCount).isEqualTo(1);

      // re-injecting runs @PostConstruct again but registers @PreDestroy only once
      scope.inject(instance);
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.initializeCount).isEqualTo(2);
      assertThat(instance.destroyCount).isZero();
    }

    assertThat(instance.destroyCount).isEqualTo(1);
  }

  @Test
  void separateInstancesAreEachDestroyedOnce() {
    ExternalInstance first = new ExternalInstance(null);
    ExternalInstance second = new ExternalInstance(null);

    try (BeanScope scope = BeanScope.builder().build()) {
      scope.inject(first);
      scope.inject(second);
      scope.inject(first);
    }

    assertThat(first.destroyCount).isEqualTo(1);
    assertThat(second.destroyCount).isEqualTo(1);
  }

  @Test
  void failingDestroyDoesNotStopOtherDestroys() {
    ExternalInstance instance = new ExternalInstance(null);

    try (BeanScope scope = BeanScope.builder().build()) {
      scope.inject(instance);
      scope.inject(new FailingDestroyTarget());
    }

    assertThat(instance.destroyCount).isEqualTo(1);
  }

  @Test
  void autoCloseableWithoutEnablePreDestroyIsNotClosed() {
    CloseableTarget instance = new CloseableTarget();

    try (BeanScope scope = BeanScope.builder().build()) {
      scope.inject(instance);
      assertThat(instance.dependency).isSameAs(scope.get(InjectedThing.class));
    }

    assertThat(instance.closeCount).isZero();
  }

  @Test
  void injectsProviderSetOptionalAndPrototypeDependencies() {
    DependencyKinds instance = new DependencyKinds();

    try (BeanScope scope = BeanScope.builder().build()) {
      InjectedThing thing = scope.get(InjectedThing.class);
      scope.inject(instance);

      assertThat(instance.provider.get()).isSameAs(thing);
      assertThat(instance.set).containsExactly(thing);
      assertThat(instance.optional).containsSame(thing);
      assertThat(instance.optionalGeneric).isPresent();
      assertThat(instance.absentOptionalGeneric).isEmpty();
      assertThat(instance.nullableGeneric).isSameAs(instance.optionalGeneric.orElseThrow());
      assertThat(instance.absentNullableGeneric).isNull();
      assertThat(instance.methodSet).containsExactly(thing);
      assertThat(instance.methodProvider.get()).isSameAs(thing);
      assertThat(instance.methodOptionalGeneric).containsSame(instance.nullableGeneric);
      assertThat(instance.methodAbsentNullableGeneric).isNull();

      // prototype dependencies are resolved from the scope on each inject() and Provider.get()
      InjectedPrototype firstPrototype = instance.prototype;
      assertThat(firstPrototype).isNotNull();
      scope.inject(instance);
      assertThat(instance.prototype).isNotNull().isNotSameAs(firstPrototype);
      assertThat(instance.prototypeProvider.get()).isNotSameAs(instance.prototypeProvider.get());
    }
  }

  @Test
  void injectsImportedTarget() {
    ImportedTarget instance = new ImportedTarget();

    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.inject(instance)).isSameAs(instance);
      assertThat(instance.dependency()).isSameAs(scope.get(ImportedDependency.class));
    }
  }

  @Test
  void annotatedSubclassInjectsOwnAndInheritedMembers() {
    DerivedTarget instance = new DerivedTarget();

    try (BeanScope scope = BeanScope.builder().build()) {
      scope.inject(instance);
      assertThat(instance.ownDependency).isNotNull();
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.methodDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.initializedScope).isSameAs(scope);
    }

    assertThat(instance.destroyCount).isEqualTo(1);
  }

  @Test
  void unannotatedSubclassUsesNearestAnnotatedSupertype() {
    class DerivedInstance extends ExternalInstance {
      @Inject InjectedThing notInjected;

      DerivedInstance() {
        super(null);
      }
    }

    DerivedInstance instance = new DerivedInstance();
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.inject(instance)).isSameAs(instance);
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.initializedScope).isSameAs(scope);
      // members declared only on the unannotated subclass are not injected
      assertThat(instance.notInjected).isNull();
    }
  }

  @Test
  void injectWithoutInjectorThrows() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThatThrownBy(() -> scope.inject(new Object()))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("No InstanceInjector found for type: java.lang.Object");
    }
  }

  @Test
  void injectNullThrows() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThatThrownBy(() -> scope.inject(null)).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  void injectAfterCloseThrows() {
    BeanScope scope = BeanScope.builder().build();
    scope.close();

    assertThatThrownBy(() -> scope.inject(new ExternalInstance(null)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("BeanScope is closed");
  }
}
