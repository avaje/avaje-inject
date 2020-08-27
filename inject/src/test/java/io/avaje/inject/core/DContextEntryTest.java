package io.avaje.inject.core;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.core.DContextEntry;
import io.avaje.inject.core.DContextEntryBean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DContextEntryTest {

  @Test
  public void get_when_onePrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "P");
  }

  @Test(expected = IllegalStateException.class)
  public void get_when_twoPrimary() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("P", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.PRIMARY));

    entry.get(null);
  }

  @Test
  public void get_when_oneNormal() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S", null, BeanEntry.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "N");
  }


  @Test
  public void get_when_oneNormal2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("N", null, BeanEntry.NORMAL));
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

    Object bean = entry.get(null);
    assertEquals(bean, "N");
  }

  @Test(expected = IllegalStateException.class)
  public void get_when_multiSecondaryOnly() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", null, BeanEntry.SECONDARY));

    entry.get(null);
  }


  @Test
  public void get_when_multiSecondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", "a", BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }

  @Test
  public void get_when_multiSecondary_butNamed2() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.SECONDARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }

  @Test
  public void get_when_secondary_butNamed() {

    DContextEntry entry = new DContextEntry();
    entry.add(DContextEntryBean.of("S1", null, BeanEntry.PRIMARY));
    entry.add(DContextEntryBean.of("S2", "b", BeanEntry.SECONDARY));

    Object bean = entry.get("b");
    assertEquals(bean, "S2");
  }
}
