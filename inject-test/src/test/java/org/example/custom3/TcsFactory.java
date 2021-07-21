package org.example.custom3;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@MyThreeScope
@Factory
public class TcsFactory {

  @Bean
  TcsGreen green() {
    return new TcsGreen();
  }
}
