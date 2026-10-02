package org.example.myapp.conditional;

public class ExternalDepService {

  private final ExternalDep dep;

  public ExternalDepService(ExternalDep dep) {
    this.dep = dep;
  }

  public ExternalDep dep() {
    return dep;
  }
}
