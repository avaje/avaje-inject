package org.example.coffee.parent;

import javax.inject.Named;
import javax.inject.Singleton;
import org.example.coffee.parent.sub.PetEngi;

@Named("Lite")
@Singleton
public class LightPetEngi extends PetEngi {
  @Override
  public String ignite() {
    return "lightPetEngi";
  }
}
