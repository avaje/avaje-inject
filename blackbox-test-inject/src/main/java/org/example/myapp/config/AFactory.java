package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PreDestroy;
import org.example.myapp.MyNestedDestroy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Factory
public class AFactory {

  public static AtomicInteger DESTROY_COUNT_BEAN = new AtomicInteger();
  public static AtomicInteger DESTROY_COUNT_COMPONENT = new AtomicInteger();
  public static AtomicInteger DESTROY_COUNT_AFOO = new AtomicInteger();

  public static void reset() {
    DESTROY_COUNT_BEAN.set(0);
    DESTROY_COUNT_AFOO.set(0);
    DESTROY_COUNT_COMPONENT.set(0);
  }

  @Bean(initMethod = "start", destroyMethod = "reaper().stop()")
  MyNestedDestroy lifecycle2() {
    return new MyNestedDestroy();
  }

  @PreDestroy(priority = 3000)
  void dest2(MyNestedDestroy bean) {
    DESTROY_COUNT_BEAN.incrementAndGet();
  }

//  // compiler error when type does not match any @Bean method
//  @PreDestroy(priority = 3001)
//  void destErr(Object bean) {
//
//  }

  /**
   * NO args so this is the normal factory component destroy method.
   */
  @PreDestroy
  void factoryDestroy() {
    DESTROY_COUNT_COMPONENT.incrementAndGet();
  }

  @Bean
  A0.Builder build0() {
    return new I0();
  }

  @Bean
  A1.Builder build1() {
    return new I1();
  }

  @Bean
  void andUse(A1.Builder a1Build) {
    a1Build.hashCode();
  }

  @Bean
  void ad(A0.Builder b1, A1.Builder b2) {
    b1.hashCode();
    b2.hashCode();
  }

  @Bean
  AFoo buildAfoo() throws IOException {
    return new AFoo();
  }

  @PreDestroy
  void destAfoo(AFoo bean) {
    DESTROY_COUNT_AFOO.incrementAndGet();
  }

  static class I0 implements A0.Builder {

  }
  static class I1 implements A1.Builder {

  }

  static class AFoo {

  }
}
