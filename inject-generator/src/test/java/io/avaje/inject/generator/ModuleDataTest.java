package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModuleDataTest {

  @Test
  void splitTopLevel_plain() {
    assertThat(ModuleData.splitTopLevel("a.A,b.B")).containsExactly("a.A", "b.B");
  }

  @Test
  void splitTopLevel_genericWithMultipleTypeArgs() {
    assertThat(ModuleData.splitTopLevel("a.A,java.util.Map<java.lang.String, java.lang.Long>:map1,b.B"))
      .containsExactly("a.A", "java.util.Map<java.lang.String, java.lang.Long>:map1", "b.B");
  }

  @Test
  void splitTopLevel_nestedGeneric() {
    assertThat(ModuleData.splitTopLevel("java.util.Map<java.lang.String, java.util.List<java.util.Map<a.A, b.B>>>,c.C"))
      .containsExactly("java.util.Map<java.lang.String, java.util.List<java.util.Map<a.A, b.B>>>", "c.C");
  }

  @Test
  void splitTopLevel_qualifierAnnotationValues() {
    assertThat(ModuleData.splitTopLevel("a.Meter:TempQualifier(defaultVal=0, inject={@Nested(inject={})}, value={CELSIUS}),b.B"))
      .containsExactly("a.Meter:TempQualifier(defaultVal=0, inject={@Nested(inject={})}, value={CELSIUS})", "b.B");
  }

  @Test
  void of_requiresColumnWithGenerics() {
    var data = ModuleData.of(new String[] {"my.Module", "a.A", "java.util.Map<java.lang.String, java.lang.Long>,b.B", " "});
    assertThat(data).isPresent();
    assertThat(data.get().requires()).containsExactly("java.util.Map<java.lang.String, java.lang.Long>", "b.B");
    assertThat(data.get().softRequires()).isEmpty();
  }
}
