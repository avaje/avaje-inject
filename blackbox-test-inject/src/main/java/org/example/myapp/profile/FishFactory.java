package org.example.myapp.profile;

import org.example.myapp.profile.Fish.Betta;
import org.example.myapp.profile.Fish.CardinalTetra;
import org.example.myapp.profile.Fish.Discus;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.avaje.inject.Secondary;

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
