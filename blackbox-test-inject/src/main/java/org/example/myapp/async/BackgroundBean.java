package org.example.myapp.async;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.AsyncBean;
import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import io.avaje.lang.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@AsyncBean
@Singleton
@Named("single")
public class BackgroundBean {

  final long initTime;

  public BackgroundBean() throws InterruptedException {
    Thread.sleep(1000);
    this.initTime = System.currentTimeMillis();
  }
}
