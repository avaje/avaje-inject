package org.example.myapp.testconfig;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import org.example.myapp.HelloData;

/**
 * Factory that builds dependencies for our "global test scope".
 * <p>
 * Typically these are relatively expensive dependencies like databases and such
 * where we desire to use docker containers, mocks, stubs etc.
 * <p>
 * For example, for AWS DynamoDB setup to use localstack docker container.
 */
@TestScope
@Factory
class ITestConfig {

  @Bean(initMethod = "onStart", destroyMethod = "onStop")
  CountTestScopeStart lifecycle() {
    return new CountTestScopeStart();
  }

  @Bean
  HelloData build() {
    return new TestHelloData();
  }

  private static class TestHelloData implements HelloData {
    @Override
    public String helloData() {
      return "TestHelloData";
    }
  }
}
