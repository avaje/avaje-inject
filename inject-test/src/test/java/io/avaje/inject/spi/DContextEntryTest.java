package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DContextEntryTest {

  @Test
  public void get_when_onePrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY));

    assertEquals(entry.get(null), "P");
  }

  @Test
  public void get_when_twoPrimary() {
    assertThrows(IllegalStateException.class, () -> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY));
      entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
      entry.add(DContextEntryBean.of("S", null, BeanEntry.PRIMARY));

      entry.get(null);
    });
  }

  @Test
  public void get_when_oneNormal() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY));

    assertEquals(entry.get(null), "N");
  }


  @Test
  public void get_when_oneNormal2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

    assertEquals(entry.get(null), "N");
  }

  @Test
  public void get_when_multiSecondaryOnly() {
    assertThrows(IllegalStateException.class, () -> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
      entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

      entry.get(null);
    });
  }


  @Test
  public void get_when_multiSecondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", "a", BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.get("b"), "S2");
  }

  @Test
  public void get_when_multiSecondary_butNamed2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.get("b"), "S2");
  }

  @Test
  public void get_when_secondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.get("b"), "S2");
  }
}
