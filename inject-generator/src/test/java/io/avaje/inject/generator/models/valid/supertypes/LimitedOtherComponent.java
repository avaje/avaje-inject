package io.avaje.inject.generator.models.valid.supertypes;

import io.avaje.inject.BeanTypes;
import io.avaje.inject.Component;

@Component
@BeanTypes(SomeInterface2.class)
public class LimitedOtherComponent extends AbstractSuperClass
    implements SomeInterface, SomeInterface2 {}
