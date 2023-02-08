module blackbox.other {

  exports org.other.one;

  requires io.avaje.inject;

  provides io.avaje.inject.spi.Module with org.other.one.OneModule;
}
