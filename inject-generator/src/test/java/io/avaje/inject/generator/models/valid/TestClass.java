package io.avaje.inject.generator.models.valid;

import io.avaje.inject.Component;
import io.avaje.inject.generator.models.valid.imported.ImportedClass;
import io.avaje.inject.generator.models.valid.imported.ImportedClassProxy;
import jakarta.inject.Singleton;

@Singleton
@Component.Import({ImportedClass.class, ImportedClassProxy.class})
public class TestClass {}
