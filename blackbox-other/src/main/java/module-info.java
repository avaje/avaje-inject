module blackbox.other {

  exports org.other.one;
  exports org.other.one.custom;
  exports org.other.one.events;
  exports org.other.one.injector;
  exports org.other.one.interweave;

  requires io.avaje.inject;
  requires io.avaje.inject.events;
  requires jakarta.inject;

  provides io.avaje.inject.spi.InjectExtension with org.other.one.OneModule;
}
