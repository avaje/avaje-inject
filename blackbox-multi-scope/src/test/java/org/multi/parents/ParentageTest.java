package org.multi.parents;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParentageTest {
  @Test
  void buildScopes() {
    try(
      BeanScope root = BeanScope.builder().modules(new Mod1Module()).build();
      BeanScope grandparent = BeanScope.builder().parent(root).modules(new Mod2Module()).build();
      BeanScope parent = BeanScope.builder().parent(grandparent).modules(new Mod3Module()).build();
      BeanScope scope = BeanScope.builder().parent(parent).modules(new Mod4Module()).build();
    ) {
      assertTrue(scope.contains(BeanIn4.class));
      assertTrue(scope.contains(BeanIn3.class));
      assertTrue(scope.contains(BeanIn2.class));
      assertTrue(scope.contains(BeanIn1.class));
      BeanIn4 beanIn4 = scope.get(BeanIn4.class);
      BeanIn3 beanIn3 = scope.get(BeanIn3.class);
      BeanIn2 beanIn2 = scope.get(BeanIn2.class);
      BeanIn1 beanIn1 = scope.get(BeanIn1.class);
      assertEquals(beanIn1, beanIn2.getBeanIn1());
      assertEquals(beanIn1, beanIn3.getBeanIn1());
      assertEquals(beanIn1, beanIn4.getBeanIn1());
      assertEquals(beanIn3, beanIn4.getBeanIn3());
      assertEquals(beanIn1, beanIn4.getBeanIn3().getBeanIn1());
    }
  }
}
