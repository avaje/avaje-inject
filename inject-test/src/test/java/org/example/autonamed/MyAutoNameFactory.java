package org.example.autonamed;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.iface.Some;

import jakarta.inject.Named;

@Factory
public class MyAutoNameFactory {

  private MyGeneric<?> wiredWild;
  private MyGeneric<?> wiredWild2;
  private MyGeneric<Some> wiredSome;

  MyGeneric<?> wiredWild() {
    return wiredWild;
  }
  MyGeneric<?> wiredWild2() {
    return wiredWild2;
  }

  MyGeneric<Some> wiredSome() {
    return wiredSome;
  }

  @Named("wild")
  @Bean
  MyGeneric<?> genericWildcard() {
    return new DMyGeneric<>();
  }

  @Named("genericString")
  @Bean
  MyGeneric<String> genericString() {
    return new DMyGeneric<>();
  }

  @Named("genericOther")
  @Bean
  MyGeneric<Some> genericWithSomeType() {
    return new DMyGeneric<>();
  }

  @Bean
  void someGenericDependency(@Named("wild") MyGeneric wild) {
    this.wiredWild = wild;
  }

  @Bean
  void someGenericDependency2(@Named("wild") MyGeneric<?> wild2) {
    this.wiredWild2 = wild2;
  }

  @Bean
  void someOtherGenericDependency(@Named("genericOther") MyGeneric<Some> some) {
    this.wiredSome = some;
  }

  @Bean
  AutoIface one() {
    return new DAutoIface("one");
  }

  @Named("one")
  @Bean
  AutoB2 oneB2() {
    return new DAutoB2("oneB2");
  }

  @Named("two")
  @Bean
  AutoB2 twoB2() {
    return new DAutoB2("twoB2");
  }

  static class DAutoB2 implements AutoB2 {
    private final String name;
    DAutoB2(String name) {
      this.name = name;
    }
    @Override
    public String who() {
      return name;
    }
  }

  static class DAutoIface implements AutoIface {

    private final String name;
    DAutoIface(String name) {
      this.name = name;
    }

    @Override
    public String who() {
      return name;
    }
  }

  static class DMyGeneric<T> implements MyGeneric<T> {

    @Override
    public T obtain() {
      return null;
    }
  }
}
