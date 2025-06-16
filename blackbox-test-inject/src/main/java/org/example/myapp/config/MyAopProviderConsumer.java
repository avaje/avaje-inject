package org.example.myapp.config;

import io.avaje.inject.Component;
import jakarta.inject.Provider;
import org.example.myapp.aspect.MyTimed;

@Component
@MyTimed
public class MyAopProviderConsumer {

  final Provider<AppConfig.Builder> builderProvider;

  public MyAopProviderConsumer(Provider<AppConfig.Builder> builderProvider) {
    this.builderProvider = builderProvider;
  }

  public AppConfig.Builder doStuff() {
    return builderProvider.get();
  }
}
