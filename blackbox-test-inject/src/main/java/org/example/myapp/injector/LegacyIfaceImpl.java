package org.example.myapp.injector;

/** A concrete implementation an external framework would instantiate. */
public class LegacyIfaceImpl implements LegacyIface {

  private Dep2 dep2;

  @Override
  public void setDep2(Dep2 dep2) {
    this.dep2 = dep2;
  }

  public Dep2 dep2() {
    return dep2;
  }
}
