package org.example.generic;

import javax.inject.Singleton;

import java.nio.file.Path;

@Singleton
public class PathBuildTask implements BuildTaskWith<Path> {

  @Override
  public void prepareWith(Path param) {
    // do nothing
  }

  @Override
  public void build() {
    // do nothing
  }

}
