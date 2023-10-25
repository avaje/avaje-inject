package org.example.myapp.config;

import io.avaje.inject.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.myapp.HelloData;
import org.example.myapp.MyDestroyOrder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Factory
public class AppConfig {

  public static AtomicBoolean BEAN_AUTO_CLOSED = new AtomicBoolean();

  @PreDestroy(priority = 999)
  void close() {
    MyDestroyOrder.add("AppConfig");
  }

  @Bean(autoCloseable = true)
  SomeInterface someInterface() {
    return new SomeInterfaceWithClose();
  }

  @Bean(destroyMethod = "shutdown", destroyPriority = 1500)
  HelloData data() {
    return new AppHelloData();
  }

  private static class AppHelloData implements HelloData {

    @Override
    public String helloData() {
      return "AppHelloData";
    }

    @Override
    public void shutdown() {
      MyDestroyOrder.add("AppHelloData");
    }
  }

  @Prototype
  @Bean
  public Builder newBuilder() {
    return new Builder();
  }

  @Bean
  Generated newGenerated() {
    return new Generated();
  }

  @Secondary
  @Bean
  public MySecType generalSecondary() {
    return new MySecType();
  }

  @Secondary
  @Bean
  public Optional<MySecOptType> optionalSecondary() {
    return Optional.of(new MySecOptType());
  }

  @Primary
  @Bean
  public MyPrim primaryBean() {
    return new MyPrim("prime");
  }

  @Named("notPrimary")
  @Bean
  public MyPrim notPrimaryBean() {
    return new MyPrim("notPrimary");
  }

  @Bean
  public Provider provider() {
    return new Provider();
  }

  @Bean
  public MyGen<String> myGen() {
    return new MyGen<>();
  }

  @Bean
  MyInterface myInterface2() {
    return new MyInterface() {};
  }

  @Bean
  MyAbstract myAbstract() {
    return new MyAbstract() {};
  }

  public static class Builder {
  }

  public static class Generated {
  }

  public static class MySecType {
  }

  public static class MySecOptType {
  }

  public static class Provider {

  }

  public static class MyGen<T> {

  }

  public interface MyInterface {

  }

  public interface SomeInterface {

  }

  private static class SomeInterfaceWithClose implements SomeInterface, AutoCloseable {

    SomeInterfaceWithClose() {
      BEAN_AUTO_CLOSED.set(false);
    }

    @Override
    public void close() {
      BEAN_AUTO_CLOSED.set(true);
    }
  }


  public static class MyPrim {
    public final String val;
    public MyPrim(String val) {
      this.val = val;
    }
  }

  public abstract class MyAbstract {

  }

  @Component
  public static class BuilderUser {

    final jakarta.inject.Provider<Builder> builderProvider;

    @Inject
    public BuilderUser(jakarta.inject.Provider<Builder> builderProvider) {
      this.builderProvider = builderProvider;
    }

    public Builder createBuilder() {
      return builderProvider.get();
    }
  }
}
