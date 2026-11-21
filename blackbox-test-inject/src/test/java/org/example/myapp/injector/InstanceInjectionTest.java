package org.example.myapp.injector;

import io.avaje.inject.BeanScope;
import org.other.one.injector.ImportedDependency;
import org.other.one.injector.ImportedTarget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

      scope.inject(instance);
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
    }

    assertThat(instance.destroyCount).isEqualTo(1);
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
  void injectsSuperclassTarget() {
    class DerivedInstance extends ExternalInstance {
      DerivedInstance() {
        super(null);
      }
    }

    DerivedInstance instance = new DerivedInstance();
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.inject(instance)).isSameAs(instance);
      assertThat(instance.fieldDependency).isSameAs(scope.get(InjectedThing.class));
      assertThat(instance.initializedScope).isSameAs(scope);
    }
  }
}
