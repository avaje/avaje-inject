package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;


import static io.avaje.inject.generator.IncludeAnnotations.include;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IncludeAnnotationsTest {

  @Test
  public void include_other_annotations() {
    assertTrue(include("foo.Bar"));
    assertTrue(include("bar.Foo"));
  }

  @Test
  public void exclude_lombok() {
    assertFalse(include("lombok.Data"));
    assertFalse(include("lombok.Getter"));
  }

  @Test
  public void exclude_kotlinMetaData() {
    assertFalse(include(Constants.KOTLIN_METADATA));
  }

  @Test
  public void exclude_di_annotations() {
    assertFalse(include("javax.annotation.Generated"));
    assertFalse(include("foo.Generated"));
    assertFalse(include(Constants.SINGLETON));
    assertFalse(include("jakarta.inject.Named"));
    assertFalse(include(Constants.FACTORY));
    assertFalse(include("io.avaje.inject.Primary"));
    assertFalse(include("io.avaje.inject.Secondary"));
    assertFalse(include(Constants.PATH));
  }

}
