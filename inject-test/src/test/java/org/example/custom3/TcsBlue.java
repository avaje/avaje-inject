package org.example.custom3;

import org.example.custom2.OcsThree;

@MyThreeScope
public class TcsBlue {

  /**
   * Wire this dependency from the parent BeanScope.
   */
  final OcsThree ocs;

  public TcsBlue(OcsThree ocs) {
    this.ocs = ocs;
  }

  public OcsThree getDependency() {
    return ocs;
  }
}
