package org.example.myapp.conditional;

public class MetaExternalDepService {

  private final MetaExternalDep dep;

  public MetaExternalDepService(MetaExternalDep dep) {
    this.dep = dep;
  }

  public MetaExternalDep dep() {
    return dep;
  }
}
