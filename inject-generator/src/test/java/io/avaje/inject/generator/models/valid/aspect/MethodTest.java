package io.avaje.inject.generator.models.valid.aspect;

import java.util.Map;

import io.avaje.inject.Component;
import io.avaje.inject.aop.AOPFallback;
import io.avaje.inject.generator.models.valid.Timed;

@Component
public class MethodTest {

  @Timed
  void test(@Param Map<@TypeUse String, String> str, @Param int inty, String regular) {}

  @Timed
  @AOPFallback("test")
  void retry(@Param Map<@TypeUse String, String> str, @Param int inty, String regular) {}

  @Timed
  @AOPFallback("retry")
  void retry2(Throwable ex) {}

  @AOPFallback("retry2")
  void retry3() {}

  @Timed
  void test2(@Param Map<@TypeUse String, String> str, @Param int inty, String regular) {}

  @AOPFallback("test2")
  void retry4(
      @Param Map<@TypeUse String, String> str, @Param int inty, String regular, Throwable t) {}

  @Timed
  void test2( @Param int inty, String regular) {}

  @AOPFallback(value = "test2", place = 1)
  void retry4(@Param int inty, String regular, Throwable t) {}
}
