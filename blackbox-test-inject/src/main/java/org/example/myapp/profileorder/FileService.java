package org.example.myapp.profileorder;

import io.avaje.inject.Profile;
import jakarta.inject.Singleton;

@Singleton
@Profile(none = "cloud")
public class FileService implements MyService {

  @Override
  public String name() {
    return "file";
  }
}
