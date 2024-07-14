package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyTest {

  @Test
  void dependsOn_soft() {
    Dependency dependency = new Dependency("org.foo.Bar",null, true);

    assertThat(dependency.isSoftDependency()).isTrue();
    assertThat(dependency.name()).isEqualTo("org.foo.Bar");
    assertThat(dependency.dependsOn()).isEqualTo("soft:org.foo.Bar");

    Dependency dependencyFromMeta = new Dependency("soft:org.foo.Bar", null);
    assertThat(dependencyFromMeta.name()).isEqualTo("org.foo.Bar");
    assertThat(dependencyFromMeta.isSoftDependency()).isTrue();
    assertThat(dependencyFromMeta.name()).isEqualTo(dependency.name());
    assertThat(dependencyFromMeta.dependsOn()).isEqualTo(dependency.dependsOn());
  }

  @Test
  void dependsOn_notSoft() {
    Dependency dependency = new Dependency("org.foo.Bar", null);

    assertThat(dependency.dependsOn()).isEqualTo("org.foo.Bar");
    assertThat(dependency.isSoftDependency()).isFalse();
    assertThat(dependency.name()).isEqualTo("org.foo.Bar");
  }

  @Test
  void dependsOn_qualifier() {
    Dependency dependency = new Dependency("org.foo.Bar", "sus");

    assertThat(dependency.dependsOn()).isEqualTo("org.foo.Bar");
    assertThat(dependency.isSoftDependency()).isFalse();
    assertThat(dependency.name()).isEqualTo("org.foo.Bar:sus");
  }
}
