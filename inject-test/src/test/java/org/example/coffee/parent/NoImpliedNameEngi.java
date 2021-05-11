package org.example.coffee.parent;

import javax.inject.Singleton;
import org.example.coffee.parent.sub.PetEngi;

/**
 * No implied name due to not ending with PetEngi.
 */
@Singleton
public class NoImpliedNameEngi extends PetEngi {
  @Override
  public String ignite() {
    return "NoImpliedNameEngi";
  }
}
