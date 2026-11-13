package io.avaje.inject.generator.models.valid.autofactory.pkg;

public class PkgHolder {

  public interface Visible {
    String visible();
  }

  private interface Hidden {
    interface AlsoHidden {}
  }
}
