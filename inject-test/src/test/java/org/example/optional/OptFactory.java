package org.example.optional;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import javax.inject.Named;

import java.util.Optional;

@Factory
class OptFactory {

  @Named("frodo")
  @Bean
  Que frodoQueue() {
    return new BasicQue("frodo");
  }

  @Named("sam")
  @Bean
  Que samQueue() {
    return new BasicQue("sam");
  }

  @Named("bilbo")
  @Bean
  Optional<Que> bilboQueue() {
    // intentional for testing optional dependency
    return Optional.empty();
  }

  @Bean
  Optional<NoImpHere> anOptional() {
    return Optional.empty();
  }

}
