package org.example.myapp.profileorder;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;

@Factory
@Profile("cloud")
public class PoolFactory {

  @Bean
  Conn conn() {
    return new Conn() {};
  }

  @Bean
  Pool pool(Conn conn) {
    return new Pool();
  }
}
