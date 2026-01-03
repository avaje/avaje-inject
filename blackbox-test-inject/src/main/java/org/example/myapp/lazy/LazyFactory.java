package org.example.myapp.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import jakarta.inject.Named;

@Lazy
@Factory
public class LazyFactory {

  @Bean
  @Named("factory")
  LazyBean lazyInt(@Nullable AtomicBoolean initialized) throws Exception {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyBean();
  }

  @Bean
  @Named("factory")
  LazyInterface lazyInterFace(@Nullable AtomicBoolean initialized) throws Exception {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyImpl(initialized);
  }

  @Bean(destroyMethod = "shutdown")
  @BeanTypes(LazyInterface.class)
  @Named("factoryBeanType")
  LazyImpl factoryBeanType(@Nullable AtomicBoolean initialized) throws Exception {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyImpl(initialized);
  }

  @Bean
  @Named("factoryThrows")
  LazyBean lazyIntThrows(@Nullable AtomicBoolean initialized) throws Exception {
    return lazyInt(initialized);
  }
}
