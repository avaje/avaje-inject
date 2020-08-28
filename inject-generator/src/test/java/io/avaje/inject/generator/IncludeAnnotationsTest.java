package io.avaje.inject.generator;

import io.avaje.inject.Factory;
import io.avaje.inject.Primary;
import io.avaje.inject.Secondary;
import org.junit.jupiter.api.Test;

import javax.inject.Named;
import javax.inject.Singleton;

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
    assertFalse(include(Singleton.class.getName()));
    assertFalse(include(Named.class.getName()));
    assertFalse(include(Factory.class.getName()));
    assertFalse(include(Primary.class.getName()));
    assertFalse(include(Secondary.class.getName()));
    assertFalse(include(Constants.PATH));
  }

}
