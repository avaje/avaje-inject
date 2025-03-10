package io.avaje.inject.test;

import java.lang.invoke.MethodHandles.Lookup;

/** Provides a Lookup instance for accessing test fields. */
public interface LookupProvider {

  /** Return the Lookup. */
  Lookup provideLookup();
}
