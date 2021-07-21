package org.example.coffee;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import org.example.coffee.core.DuperPump;
import org.example.coffee.list.BSomei;
import org.example.coffee.list.Somei;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class CoffeeMakerTest {

  @Test
  public void makeIt_via_SystemContext() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      Pump pump = context.get(Pump.class);
      assertThat(pump).isInstanceOf(DuperPump.class);
    }
  }

  @Test
  public void makeIt_via_BootContext_withNoShutdownHook() {
    try (BeanScope context = BeanScope.newBuilder()
      .withShutdownHook(false)
      .build()) {

      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

  @Test
  public void makeIt_via_BootContext() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      final List<BeanEntry> beanEntries = context.all();
      assertThat(beanEntries).hasSizeGreaterThan(10);

      // all entries for an interface
      final List<BeanEntry> someiEntries = beanEntries.stream()
        .filter(beanEntry -> beanEntry.hasKey(Somei.class))
        .collect(toList());
      assertThat(someiEntries).hasSize(3);

      final Optional<BeanEntry> bsomeEntry = someiEntries.stream().filter(entry -> entry.type().equals(BSomei.class)).findFirst();
      assertThat(bsomeEntry).isPresent();

      final BeanEntry entry = bsomeEntry.get();
      assertThat(entry.qualifierName()).isEqualTo("b");
      assertThat(entry.keys()).containsExactly(BSomei.class.getCanonicalName(), Somei.class.getCanonicalName());
      assertThat(entry.type()).isEqualTo(BSomei.class);
      assertThat(entry.priority()).isEqualTo(0);
      assertThat(entry.bean()).isEqualTo(context.get(Somei.class, "b"));
      assertThat(entry.bean()).isEqualTo(context.get(BSomei.class));
    }
  }

}
