package org.example.inherit;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.inherit.notpublic.PubExposed;

@Factory
public class InhPub  {

  @Bean
  PubExposed exposed() {
    return new PubExposed();
  }
}
