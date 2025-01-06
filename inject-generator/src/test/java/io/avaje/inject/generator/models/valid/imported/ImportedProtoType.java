package io.avaje.inject.generator.models.valid.imported;

import io.avaje.inject.Component;
import io.avaje.inject.Component.Import.Kind;

@Component.Import(value = ImportedProtoType.class, kind =  Kind.PROTOTYPE)
public class ImportedProtoType {}
