package io.avaje.inject.generator.models.valid.autofactory.pkg;

import io.avaje.inject.AutoFactory;
import io.avaje.inject.generator.models.valid.autofactory.MyCreator;

@AutoFactory(MyCreator.class)
public interface PkgOwnFactory {

  String find(long id);
}
