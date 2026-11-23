package org.other.one.injector;

import jakarta.inject.Inject;

public final class ImportedTarget {

  private ImportedDependency dependency;

  @Inject
  public void setDependency(ImportedDependency dependency) {
    this.dependency = dependency;
  }

  public ImportedDependency dependency() {
    return dependency;
  }
}
