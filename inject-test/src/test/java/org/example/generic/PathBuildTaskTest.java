package org.example.generic;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PathBuildTaskTest {

  @Test
  void genericInterfaceWithExtends_expect_providesExtendedInterface() {

    try (BeanScope beanScope = BeanScope.newBuilder().build()) {

      PathBuildTask pathBuildTask = beanScope.get(PathBuildTask.class);
      BuildTask buildTask = beanScope.get(BuildTask.class);

      // PathBuildTask is registered as providing PathBuildTask.class, BuildTask.class and generic TYPE_BuildTaskWithPath
      assertThat(pathBuildTask).isSameAs(buildTask);

      Object viaType = beanScope.get(PathBuildTask$DI.TYPE_BuildTaskWithPath, null);
      assertThat(viaType).isSameAs(buildTask);
    }
  }
}
