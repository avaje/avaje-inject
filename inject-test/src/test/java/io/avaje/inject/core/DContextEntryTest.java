package io.avaje.inject.core;

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

    assertEquals(entry.candidate(null).getBean(), "P");
  }

  @Test
  public void get_when_twoPrimary() {
    assertThrows(IllegalStateException.class, ()-> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY));
      entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
      entry.add(DContextEntryBean.of("S", null, BeanEntry.PRIMARY));

      entry.candidate(null);
    });
  }

  @Test
  public void get_when_oneNormal() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY));

    assertEquals(entry.candidate(null).getBean(), "N");
  }


  @Test
  public void get_when_oneNormal2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

    assertEquals(entry.candidate(null).getBean(), "N");
  }

  @Test
  public void get_when_multiSecondaryOnly() {
    assertThrows(IllegalStateException.class, ()-> {
      DContextEntry entry = new DContextEntry();
      entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
      entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

      entry.candidate(null);
    });
  }


  @Test
  public void get_when_multiSecondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", "a", BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.candidate("b").getBean(), "S2");
  }

  @Test
  public void get_when_multiSecondary_butNamed2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.candidate("b").getBean(), "S2");
  }

  @Test
  public void get_when_secondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    assertEquals(entry.candidate("b").getBean(), "S2");
  }
}
