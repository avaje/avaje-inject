package org.example.myapp.autofactory.pkg;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutoFactoryPackageTest {

  @Test
  void allPackageTypesCreatedByCreator() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.get(ProductMapper.class).findProduct("p1")).isEqualTo("product p1");
      assertThat(scope.get(ProductMapper.Search.class).search("shoes")).isEqualTo("search shoes");
      assertThat(scope.get(StockMapper.class).findStock("s1")).isEqualTo("stock s1");
      assertThat(scope.get(AbstractAudit.class).audit("a1")).isEqualTo("audit a1");
      assertThat(scope.get(PkgService.class).describe("x")).isEqualTo("product x | stock x");
    }
  }
}
