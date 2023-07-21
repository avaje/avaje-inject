package io.avaje.inject.generator.models.valid.profile;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.avaje.inject.Secondary;
import io.avaje.inject.generator.models.valid.profile.Fish.Betta;
import io.avaje.inject.generator.models.valid.profile.Fish.CardinalTetra;
import io.avaje.inject.generator.models.valid.profile.Fish.Discus;

@Factory
@FishProfile
@Profile(value = "factory", all = "testRepeatable", none = "neverExisted")
public class FishFactory {

  @Bean
  @Profile(none = "betta-time")
  public CardinalTetra tetra() {
    return new CardinalTetra();
  }

  @Bean
  @Secondary
  public Discus plate() {
    return new Discus();
  }

  @Bean
  @Bettas
  public Betta sweetPrince() {
    return new Betta();
  }
}
