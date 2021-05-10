package org.example.coffee.parent;

import jakarta.inject.Singleton;
import org.example.coffee.parent.sub.Engi;

@Singleton
public class DesEngi extends Engi {
  @Override
  public String ignite() {
    return "desEngi";
  }
}
