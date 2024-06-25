module io.avaje.inject.events {

  exports io.avaje.inject.events;

  requires transitive io.avaje.inject;
  provides io.avaje.inject.spi.InjectExtension with io.avaje.inject.events.spi.ObserverManagerPlugin;
}
