package org.example.factory;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class MyBasicFactory {

  @Bean(destroyMethod = "close")
  OutIFace create() {
    return new OutImpl();
  }

  static class OutImpl implements OutIFace {

    @Override
    public void doIt() {

    }

    @Override
    public void close() {

    }

    @Override
    public String source() {
      return null;
    }
  }
}



