package io.avaje.inject.generator.models.valid.aspect;

import java.util.Map;

import io.avaje.inject.Component;
import io.avaje.inject.generator.models.valid.Timed;

@Component
public class MethodTest {

  @Timed
  void test(@Param Map<@TypeUse String, String> str, @Param int inty, String regular) {}
}
