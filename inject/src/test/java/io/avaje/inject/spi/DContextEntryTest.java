package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertThrows(
        IllegalStateException.class,
        () -> {
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
  void get_when_currentModule() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", "same", BeanEntry.NORMAL, Module.class));
    entry.add(DContextEntryBean.of("N", "same", BeanEntry.NORMAL, null));

    assertEquals(entry.get("same", Module.class), "N");
    assertEquals(entry.get("same", null), "N");
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

    assertThrows(
        IllegalStateException.class,
        () -> {
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
}
