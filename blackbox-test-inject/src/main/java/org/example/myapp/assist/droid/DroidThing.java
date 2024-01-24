package org.example.myapp.assist.droid;

import io.avaje.inject.Component;
import jakarta.inject.Named;

@Component
public class DroidThing {


  final DroidFactory factoryViaField;

  final DroidFactory factoryViaMethod;


  final DroidFactory factoryViaConstructor;

  public DroidThing(@Named("androidfield") DroidFactory factoryViaField,
                    @Named("androidmethod") DroidFactory factoryViaMethod,
                    @Named("android") DroidFactory factoryViaConstructor) {
    this.factoryViaField = factoryViaField;
    this.factoryViaMethod = factoryViaMethod;
    this.factoryViaConstructor = factoryViaConstructor;
  }

  public DroidFactory.Droid viaField(int p, Model m) {
    return factoryViaField.createDroid(p, m);
  }

  public DroidFactory.Droid viaMethod(int p, Model m) {
    return factoryViaMethod.createDroid(p, m);
  }
  public DroidFactory.Droid viaCtor(int p, Model m) {
    return factoryViaConstructor.createDroid(p, m);
  }
}
