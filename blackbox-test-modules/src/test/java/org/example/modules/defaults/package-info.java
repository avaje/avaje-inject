/**
 * Adds to the default modules from {@code @TestModules} on {@code TestConfig}. This package has
 * no beans, so this is only read as a package-info.
 */
@TestModules(ReportModule.class)
package org.example.modules.defaults;

import io.avaje.inject.test.TestModules;
import org.example.modules.report.ReportModule;
