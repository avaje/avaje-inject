package io.avaje.inject.generator.models.valid.external;

import java.lang.ref.WeakReference;

import io.avaje.inject.External;
import jakarta.inject.Singleton;

@Singleton
public class BulkExternal {
  @External
  public BulkExternal(WeakReference<Integer> mace, Cloneable jango) {}
}
