open module blackbox.test.inject {

  requires blackbox.aspect;
  requires blackbox.other;
  requires io.avaje.inject;
  requires io.github.resilience4j.retry;
  requires io.avaje.jsonb;
  requires vavr;

  provides io.avaje.inject.spi.Module with org.example.myapp.MyappModule;
}
