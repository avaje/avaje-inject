package org.example.myapp.beantypes;

import org.example.myapp.config.AppConfig.SomeInterface;
import org.other.one.SomeOptionalDep;

import io.avaje.inject.BeanTypes;
import io.avaje.inject.Component;
import jakarta.inject.Named;

@Component
@Named("type")
@BeanTypes(AbstractSuperClass.class)
public class BeanTypeComponent extends AbstractSuperClass
    implements SomeInterface, SomeOptionalDep, LimitedInterface {}
