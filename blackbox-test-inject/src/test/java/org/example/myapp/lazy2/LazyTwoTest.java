package org.example.myapp.lazy2;

import io.avaje.inject.BeanScope;
import org.example.myapp.HelloService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LazyTwoTest {

  @Test
  void test() {
    try (var scope = BeanScope.builder().build()) {
      assertThat(LazyTwo.INIT).isFalse();
      assertThat(LazyOneA.AINIT).describedAs("Only 1 constructor, so <init> called by LazyOneA$$Lazy()").isTrue();
      assertThat(LazyOneA.A_POST_CONSTRUCT).isFalse();
      assertThat(LazyOneB.BINIT).isFalse();

      var lazyOneA = scope.get(LazyOneA.class);
      assertThat(LazyOneA.A_POST_CONSTRUCT).describedAs("Only got the proxy").isFalse();

      var lazy = scope.get(LazyTwo.class);
      assertThat(lazy.getClass().toString()).describedAs("got the proxy").contains("LazyTwo$Lazy");
      assertThat(LazyTwo.INIT).isFalse();
      assertThat(LazyOneB.BINIT).isFalse();
      assertThat(LazyOneA.A_POST_CONSTRUCT).describedAs("Only got the proxy").isFalse();

      assertThat(lazy.oneA()).describedAs("same proxy instance").isSameAs(lazyOneA);

      // invocation will initialize the lazy beans
      String value = lazy.something();
      assertThat(value).isEqualTo("two-oneA-oneB");
      assertThat(LazyTwo.INIT).isTrue();
      assertThat(LazyOneA.A_POST_CONSTRUCT).isTrue();
      assertThat(LazyOneB.BINIT).isTrue();

      // the graph is of Lazy beans
      String description = lazy.description();
      assertThat(description).describedAs("this is the underlying real instance").doesNotContain("LazyTwo$Lazy");
      assertThat(description).contains("LazyOneA$Lazy");
      assertThat(description).contains("LazyOneB$Lazy");

      assertThat(scope.get(LazyTwo.class)).isSameAs(lazy);

      HelloService nonLazyDependency = lazy.oneB().helloService();
      HelloService helloService = scope.get(HelloService.class);
      assertThat(nonLazyDependency).isSameAs(helloService);
    }
  }
}
