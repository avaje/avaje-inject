package org.example.myapp.other;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
public class InjectTestOnlyComponentTest {

  @Inject WireOther myTestOnlyComponent;
  @Inject WireOther2 myTestOnlyComponent2;
  @Inject WireOther3 withBeanScope;

  @Test
  void test() {
    assertThat(myTestOnlyComponent).isNotNull();
    assertThat(myTestOnlyComponent.component).isNotNull();
    assertThat(myTestOnlyComponent.plugin).isNotNull();

    assertThat(myTestOnlyComponent2).isNotNull();
    assertThat(myTestOnlyComponent2.component).isNotNull();
    assertThat(myTestOnlyComponent2.plugin).isNotNull();

    assertThat(withBeanScope).isNotNull();
    assertThat(withBeanScope.beanScope).isNotNull();
  }
}
