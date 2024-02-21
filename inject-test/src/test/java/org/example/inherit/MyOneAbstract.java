package org.example.inherit;

import jakarta.inject.Inject;
import org.example.coffee.core.Steamer;

public abstract class MyOneAbstract {

  @Inject
  Steamer steamer;
}
