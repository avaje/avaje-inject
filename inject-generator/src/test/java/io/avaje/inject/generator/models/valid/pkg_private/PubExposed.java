package io.avaje.inject.generator.models.valid.pkg_private;

/**
 * Extends a package protected class, implements package protected interface.
 * <p>
 * Neither should be including in the generated isAddBeanFor().
 */
public class PubExposed extends NonPubBase implements NonPubIface {

  @Override
  public String ifaceMethod() {
    return "ifaceMethod";
  }

}
