package org.example.coffee.parent;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.example.coffee.parent.sub.PetEngi;

@Named("Lite")
@Singleton
public class LightPetEngi extends PetEngi {
  @Override
  public String ignite() {
    return "lightPetEngi";
  }
}
