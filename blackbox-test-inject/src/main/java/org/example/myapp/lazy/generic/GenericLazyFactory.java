package org.example.myapp.lazy.generic;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import jakarta.inject.Named;

@Lazy
@Factory
public class GenericLazyFactory {

  @Bean
  @Named("factory")
  LazyGenericInterface<String> lazyInterFace(@Nullable AtomicBoolean initialized) throws Exception {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyGenericImpl(initialized);
  }

  @Bean
  @BeanTypes(LazyGenericInterface.class)
  @Named("factoryBeanType")
  LazyGenericImpl factoryBeanType(@Nullable AtomicBoolean initialized) throws Exception {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyGenericImpl(initialized);
  }
}
