package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("all")
class DContextEntryTest {

  @Test
  void get_when_onePrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY, null));
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY, null));

    assertEquals(entry.get(null, null), "P");
  }

  @Test
  void get_when_twoPrimary() {
    assertThrows(IllegalStateException.class, () -> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY, null));
      entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL, null));
      entry.add(DContextEntryBean.of("S", null, BeanEntry.PRIMARY, null));

      entry.get(null, null);
    });
  }

  @Test
  void get_when_oneNormal() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY, null));

    assertEquals(entry.get(null, null), "N");
  }


  @Test
  void get_when_oneNormal2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY, null));
    entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY, null));

    assertEquals(entry.get(null, null), "N");
  }

  @Test
  void get_when_multiSecondaryOnly() {

    assertThrows(IllegalStateException.class, () -> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY, null));
      entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY, null));

      entry.get(null, null);
    });
  }

  @Test
  void get_when_multiSecondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", "a", BeanEntry.SECONDARY, null));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY, null));

    assertEquals(entry.get("b", null), "S2");
  }

  @Test
  void get_when_multiSecondary_butNamed2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY, null));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY, null));

    assertEquals(entry.get("b", null), "S2");
  }

  @Test
  void get_when_secondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.PRIMARY, null));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY, null));

    assertEquals(entry.get("b", null), "S2");
  }

  @Test
  void getWithName_when_currentModule() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", "same", BeanEntry.NORMAL, InjectModule.class));
    entry.add(DContextEntryBean.of("N2", "same", BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("N3", "same", BeanEntry.NORMAL, OtherModule.class));

    assertEquals(entry.get("same", null), "N2");
    assertEquals(entry.get("same", InjectModule.class), "N1");
    assertEquals(entry.get("same", OtherModule.class), "N3");

    assertEquals(entry.get(null, null), "N2");
    assertEquals(entry.get(null, InjectModule.class), "N1");
    assertEquals(entry.get(null, OtherModule.class), "N3");
  }

  @Test
  void getWithoutName_when_currentModule() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", null, BeanEntry.NORMAL, InjectModule.class));
    entry.add(DContextEntryBean.of("N2", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("N3", null, BeanEntry.NORMAL, OtherModule.class));

    assertEquals(entry.get(null, null), "N2");
    assertEquals(entry.get(null, InjectModule.class), "N1");
    assertEquals(entry.get(null, OtherModule.class), "N3");

    assertNull(entry.get("notThere", null));
    assertNull(entry.get("notThere", InjectModule.class));
    assertNull(entry.get("notThere", OtherModule.class));
  }

  @Test
  void getWithName_expect_matchOnName() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", "same", BeanEntry.NORMAL, InjectModule.class));
    entry.add(DContextEntryBean.of("N2", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("N3", null, BeanEntry.NORMAL, OtherModule.class));

    assertEquals(entry.get("same", null), "N1");
    assertEquals(entry.get("same", InjectModule.class), "N1");
    assertEquals(entry.get("same", OtherModule.class), "N1");
  }

  @Test
  void getWithoutName_expect_matchOnNoName() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", null, BeanEntry.NORMAL, InjectModule.class));
    entry.add(DContextEntryBean.of("N2", "same", BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("N3", "same", BeanEntry.NORMAL, OtherModule.class));

    assertEquals(entry.get(null, null), "N1");
    assertEquals(entry.get(null, InjectModule.class), "N1");
    assertEquals(entry.get(null, OtherModule.class), "N1");
  }

  @Test
  void when_noQualifier_expect_matchedToNoQualider() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", null, BeanEntry.NORMAL, InjectModule.class));
    entry.add(DContextEntryBean.of("N2", "myName", BeanEntry.NORMAL, InjectModule.class));

    assertEquals(entry.get("myName", InjectModule.class), "N2");
    assertEquals(entry.get(null, InjectModule.class), "N1");
  }

  @Test
  void when_noQualifier_expect_matchedToNoQualider2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N1", null, BeanEntry.NORMAL, null));
    entry.add(DContextEntryBean.of("N2", "myName", BeanEntry.NORMAL, null));

    assertEquals(entry.get("myName", InjectModule.class), "N2");
    assertEquals(entry.get(null, InjectModule.class), "N1");
  }

  class OtherModule implements InjectModule {
    @Override
    public Class<?>[] classes() {
      return new Class[0];
    }
    @Override
    public void build(Builder builder) {
    }
  }
}
