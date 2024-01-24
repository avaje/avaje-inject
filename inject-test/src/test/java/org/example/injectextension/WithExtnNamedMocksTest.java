package org.example.injectextension;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectExtension;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.coffee.qualifier.ColorStore;
import org.example.coffee.qualifier.SomeStore;
import org.example.coffee.qualifier.StoreManagerWithSetterQualifier;
import org.example.coffee.qualifier.ColorStore.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(InjectExtension.class)
class WithExtnNamedMocksTest {

  @Mock
  @ColorStore(Color.BLUE)
  SomeStore blueStore;

  @Mock @Named("green") SomeStore greenStore;

  @Inject StoreManagerWithSetterQualifier storeManager;

  @Test
  void when_plainCaptor() {
    when(blueStore.store()).thenReturn("BlueStoreStub");
    when(greenStore.store()).thenReturn("GreenStoreStub");

    assertThat(storeManager.blueStore()).isEqualTo("BlueStoreStub");
    assertThat(storeManager.greenStore()).isEqualTo("GreenStoreStub");
  }

  static class ProgrammaticTest {

    @Test
    void test() {

      try (BeanScope beanScope = BeanScope.builder()
        .forTesting()
        .mock(SomeStore.class, "Blue")
        .mock(SomeStore.class, "green")
        .build()) {

        final SomeStore greenStore = beanScope.get(SomeStore.class, "green");
        final SomeStore blueStore = beanScope.get(SomeStore.class, "blue");
        when(blueStore.store()).thenReturn("BlueStoreStub");
        when(greenStore.store()).thenReturn("GreenStoreStub");

        final StoreManagerWithSetterQualifier storeManager = beanScope.get(StoreManagerWithSetterQualifier.class);
        assertThat(storeManager.blueStore()).isEqualTo("BlueStoreStub");
        assertThat(storeManager.greenStore()).isEqualTo("GreenStoreStub");
      }

    }
  }
}
