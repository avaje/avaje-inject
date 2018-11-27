package io.dinject.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class DContextEntryTest {

  @Test
  public void get_when_onePrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, Flag.PRIMARY));
    entry.add(DContextEntryBean.of("N", null, Flag.NORMAL));
    entry.add(DContextEntryBean.of("S", null, Flag.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "P");
  }

  @Test(expected = IllegalStateException.class)
  public void get_when_twoPrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, Flag.PRIMARY));
    entry.add(DContextEntryBean.of("N", null, Flag.NORMAL));
    entry.add(DContextEntryBean.of("S", null, Flag.PRIMARY));

    entry.get(null);
  }

  @Test
  public void get_when_oneNormal() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, Flag.NORMAL));
    entry.add(DContextEntryBean.of("S", null, Flag.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "N");
  }


  @Test
  public void get_when_oneNormal2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, Flag.NORMAL));
    entry.add(DContextEntryBean.of("S1", null, Flag.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, Flag.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "N");
  }

  @Test(expected = IllegalStateException.class)
  public void get_when_multiSecondaryOnly() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, Flag.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, Flag.SECONDARY));

    entry.get(null);
  }


  @Test
  public void get_when_multiSecondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", "a", Flag.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", Flag.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }

  @Test
  public void get_when_multiSecondary_butNamed2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, Flag.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", Flag.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }

  @Test
  public void get_when_secondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, Flag.PRIMARY));
    entry.add(DContextEntryBean.of("S2", "b", Flag.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }
}
