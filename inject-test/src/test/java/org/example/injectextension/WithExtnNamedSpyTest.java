package org.example.injectextension;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectJunitExtension;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.coffee.qualifier.Blue;
import org.example.coffee.qualifier.SomeStore;
import org.example.coffee.qualifier.StoreManagerWithSetterQualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(InjectJunitExtension.class)
class WithExtnNamedSpyTest {

  @Spy @Blue
  SomeStore blueStore;

  @Spy @Named("green")
  SomeStore greenStore;

  @Inject
  StoreManagerWithSetterQualifier storeManager;

  @Test
  void spy_verify() {

    assertThat(storeManager.greenStore()).isEqualTo("green");
    verify(greenStore).store();

    assertThat(storeManager.blueStore()).isEqualTo("blue");
    verify(blueStore).store();
  }

  static class ProgrammaticTest {

    @Test
    void test() {

      try (BeanScope beanScope = BeanScope.builder()
        .forTesting()
        .spy(SomeStore.class, "blue")
        .spy(SomeStore.class, "green")
        .build()) {

        final StoreManagerWithSetterQualifier storeManager = beanScope.get(StoreManagerWithSetterQualifier.class);
        assertThat(storeManager.blueStore()).isEqualTo("blue");

        final SomeStore blueStore = beanScope.get(SomeStore.class, "blue");
        verify(blueStore).store();

        assertThat(storeManager.greenStore()).isEqualTo("green");
        final SomeStore greenStore = beanScope.get(SomeStore.class, "green");
        verify(greenStore).store();
      }

    }
  }
}
