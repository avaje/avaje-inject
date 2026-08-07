package org.example.myapp.profileorder;

import io.avaje.inject.Profile;
import jakarta.inject.Singleton;

@Singleton
@Profile("cloud")
public class DbService implements MyService {

  public DbService(Pool pool) {}

  @Override
  public String name() {
    return "db";
  }
}
