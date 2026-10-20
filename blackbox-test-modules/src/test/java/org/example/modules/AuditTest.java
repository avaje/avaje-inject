package org.example.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.example.modules.audit.AuditModule;

import io.avaje.inject.test.InjectTest;

/** Composed annotation sharing the selected modules across tests. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@InjectTest(modules = AuditModule.class)
@interface AuditTest {
}
