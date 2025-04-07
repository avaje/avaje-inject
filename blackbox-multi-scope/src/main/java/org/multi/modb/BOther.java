package org.multi.modb;

import org.multi.modc.modb.COther;
import org.multi.scope.ModBScope;

@ModBScope
public class BOther {

  private final COther cOther;

  public BOther(COther cOther) {
    this.cOther = cOther;
  }
}
