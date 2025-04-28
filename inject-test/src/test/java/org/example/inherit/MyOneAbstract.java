package org.example.inherit;

import jakarta.inject.Inject;
import org.example.coffee.core.Steamer;
import org.example.inherit.sub.MySubAbstract;

public abstract class MyOneAbstract extends MySubAbstract {

  @Inject
  Steamer steamer;
}
