@InjectModule(provides = {SomeOptionalDep.class, AtomicBoolean.class})
package org.other.one;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.InjectModule;
