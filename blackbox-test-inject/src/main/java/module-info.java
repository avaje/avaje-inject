import org.example.myapp.ConfigPropertiesPlugin;
import org.example.myapp.MyServiceIface;
import org.example.myapp.MyServiceImpl;

import io.avaje.inject.spi.InjectExtension;

module blackbox.test.inject {

  requires blackbox.aspect;
  requires blackbox.other;
  requires io.avaje.config;
  requires io.avaje.inject;
  requires io.avaje.inject.aop;
  requires io.avaje.inject.events;
  requires io.avaje.jsonb;
  requires io.github.resilience4j.annotations;
  requires io.github.resilience4j.core;
  requires io.github.resilience4j.retry;
  requires java.desktop;
  requires org.jspecify;
  requires vavr;

  requires static io.avaje.spi;

  provides InjectExtension with org.example.myapp.CompiledOrder, ConfigPropertiesPlugin, org.example.myapp.MyappModule;
  provides MyServiceIface with MyServiceImpl;
}
