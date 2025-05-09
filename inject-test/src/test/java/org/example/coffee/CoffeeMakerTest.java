package org.example.coffee;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import org.example.coffee.core.DuperPump;
import org.example.coffee.generic.HazRepo;
import org.example.coffee.generic.HazRepo$DI;
import org.example.coffee.generic.Repository;
import org.example.coffee.list.BSomei;
import org.example.coffee.list.Somei;
import org.example.coffee.provider.AProv;
import org.example.coffee.provider.AProvProvider;
import org.example.iface.*;
import org.example.inherit.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class CoffeeMakerTest {

  @Test
  void makeIt_via_SystemContext() {
    try (BeanScope context = BeanScope.builder().build()) {
      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      Pump pump = context.get(Pump.class);
      assertThat(pump).isInstanceOf(DuperPump.class);
    }
  }

  @Test
  void makeIt_via_BootContext_withNoShutdownHook() {
    try (BeanScope context = BeanScope.builder()
      .shutdownHook(false)
      .build()) {

      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

  @Test
  void beanScope_all() {
    try (BeanScope context = BeanScope.builder().build()) {
      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      final List<BeanEntry> beanEntries = context.all();
      assertThat(beanEntries).hasSizeGreaterThan(10);

      // all entries for an interface
      final List<BeanEntry> someiEntries = beanEntries.stream()
        .filter(beanEntry -> beanEntry.hasKey(Somei.class))
        .collect(toList());
      assertThat(someiEntries).hasSize(3);

      final Optional<BeanEntry> bsomeEntry = someiEntries.stream()
        .filter(entry -> entry.type().equals(BSomei.class)).findFirst();
      assertThat(bsomeEntry).isPresent();

      final BeanEntry entry = bsomeEntry.get();
      assertThat(entry.qualifierName()).isEqualTo("B");
      assertThat(entry.keys()).containsExactlyInAnyOrder(name(BSomei.class), name(Somei.class));
      assertThat(entry.type()).isEqualTo(BSomei.class);
      assertThat(entry.priority()).isEqualTo(0);
      assertThat(entry.bean()).isEqualTo(context.get(Somei.class, "B"));
      assertThat(entry.bean()).isEqualTo(context.get(BSomei.class));
    }
  }

  @Test
  void beanScope_all_superClasses() {
    try (BeanScope context = BeanScope.builder().build()) {

      final List<BeanEntry> beanEntries = context.all();

      final BeanEntry inhEntry = beanEntries.stream()
        .filter(e -> e.hasKey(InhOne.class))
        .findFirst().orElse(null);

      assertThat(inhEntry.keys())
        .containsExactly(name(InhOne.class), name(InhBase.class), name(InhBaseBase.class),
          name(InhBaseIface2.class), name(InhBaseIface3.class), name(InhBaseIface.class));
    }
  }

  @Test
  void beanScope_all_interfaces() {
    try (BeanScope context = BeanScope.builder().build()) {

      final List<BeanEntry> beanEntries = context.all();

      final BeanEntry extendIfaces = beanEntries.stream()
        .filter(e -> e.hasKey(ConcreteExtend.class))
        .findFirst().orElse(null);

      assertThat(extendIfaces.keys())
        .containsExactly(name(ConcreteExtend.class), name(IfaceExtend.class), name(IfaseBase.class));
    }
  }

  @Test
  void beanScope_all_includesGenericInterfaces() {
    try (BeanScope context = BeanScope.builder().build()) {

      final List<BeanEntry> beanEntries = context.all();

      final BeanEntry hazRepo = beanEntries.stream()
        .filter(e -> e.hasKey(HazRepo.class))
        .findFirst().orElse(null);

      assertThat(hazRepo.keys())
        .containsExactly(name(HazRepo.class), name(HazRepo$DI.TYPE_RepositoryHazLong), name(Repository.class));
    }
  }

  @Test
  void beanScope_all_interfaceWithParameter() {
    try (BeanScope context = BeanScope.builder().build()) {

      final List<BeanEntry> beanEntries = context.all();

      final BeanEntry hazRepo = beanEntries.stream()
        .filter(e -> e.hasKey(MyParam.class))
        .findFirst().orElse(null);

      assertThat(hazRepo.keys())
        .containsExactly(name(MyParam.class), name(IfaceParam.class), name(IfaceParamParent.class));
    }
  }

  @Test
  void beanScope_all_provider() {
    try (BeanScope context = BeanScope.builder().build()) {

      final List<BeanEntry> beanEntries = context.all();

      final BeanEntry extendIfaces = beanEntries.stream()
        .filter(e -> e.hasKey(AProvProvider.class))
        .findFirst().orElse(null);

      assertThat(extendIfaces.keys())
        .containsExactly(name(AProvProvider.class), name(AProv.class));
    }
  }

  String name(Type cls) {
    return cls.getTypeName();
  }

}
