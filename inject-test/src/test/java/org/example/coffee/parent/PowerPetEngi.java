package org.example.coffee.parent;

import javax.inject.Singleton;
import org.example.coffee.parent.sub.PetEngi;

@Singleton
public class PowerPetEngi extends PetEngi {
  @Override
  public String ignite() {
    return "powerPetEngi";
  }
}
