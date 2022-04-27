package org.example.inherit.notpublic;

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
