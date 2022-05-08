package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.GenericMaster;
import org.mockito.plugins.MemberAccessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class MetaReader {

  final List<Field> captors = new ArrayList<>();
  final List<FieldTarget> mocks = new ArrayList<>();
  final List<FieldTarget> spies = new ArrayList<>();
  final List<FieldTarget> injection = new ArrayList<>();
  final List<FieldTarget> staticMocks = new ArrayList<>();
  final List<FieldTarget> staticSpies = new ArrayList<>();
  final List<FieldTarget> staticInjection = new ArrayList<>();
  boolean classInjection;
  boolean instanceInjection;

  MetaReader(Class<?> testClass) {
    for (Field field : testClass.getDeclaredFields()) {
      readField(field);
    }
  }

  boolean hasClassInjection() {
    return classInjection;
  }

  boolean hasInstanceInjection() {
    return instanceInjection;
  }

  @Override
  public String toString() {
    String s = toStringAppend("mocks:", mocks);
    s += toStringAppend("spies:", spies);
    s += toStringAppend("inject:", injection);
    s += toStringAppend("captors:", captors);
    s += toStringAppend("staticMocks:", staticMocks);
    s += toStringAppend("staticSpies:", staticSpies);
    s += toStringAppend("staticInjection:", staticInjection);
    return s;
  }

  private String toStringAppend(String key, List<?> entries) {
    return entries.isEmpty() ? "" : key + entries + "; ";
  }

  private void readField(Field field) {
    final Mock mockAnnotation = field.getAnnotation(Mock.class);
    if (mockAnnotation != null) {
      add(newTarget(field), mocks, staticMocks);
      return;
    }
    final Spy spyAnnotation = field.getAnnotation(Spy.class);
    if (spyAnnotation != null) {
      add(newTarget(field), spies, staticSpies);
      return;
    }
    final Captor captorAnnotation = field.getAnnotation(Captor.class);
    if (captorAnnotation != null) {
      captors.add(field);
      return;
    }
    final Inject injectAnnotation = field.getAnnotation(Inject.class);
    if (injectAnnotation != null) {
      add(newTarget(field), injection, staticInjection);
    }
  }

  private void add(FieldTarget target, List<FieldTarget> instanceList, List<FieldTarget> staticList) {
    if (target.isStatic()) {
      classInjection = true;
      staticList.add(target);
    } else {
      instanceInjection = true;
      instanceList.add(target);
    }
  }

  private FieldTarget newTarget(Field field) {
    return new FieldTarget(field, name(field));
  }

  private String name(Field field) {
    final Named named = field.getAnnotation(Named.class);
    if (named != null) {
      return named.value().toLowerCase();
    }
    for (Annotation annotation : field.getAnnotations()) {
      for (Annotation metaAnnotation : annotation.annotationType().getAnnotations()) {
        if (metaAnnotation.annotationType().equals(Qualifier.class)) {
          return annotation.annotationType().getSimpleName().toLowerCase();
        }
      }
    }
    return null;
  }

  void setFromScope(BeanScope beanScope, Object testInstance) {
    if (testInstance != null) {
      setForInstance(beanScope, testInstance);
    } else {
      setForStatics(beanScope);
    }
  }

  private void setForInstance(BeanScope beanScope, Object testInstance) {
    try {
      for (Field field : captors) {
        set(field, captorFor(field), testInstance);
      }
      for (FieldTarget target : mocks) {
        target.setFromScope(beanScope, testInstance);
      }
      for (FieldTarget target : spies) {
        target.setFromScope(beanScope, testInstance);
      }
      for (FieldTarget target : injection) {
        target.setFromScope(beanScope, testInstance);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setForStatics(BeanScope beanScope) {
    try {
      for (FieldTarget target : staticMocks) {
        target.setFromScope(beanScope, null);
      }
      for (FieldTarget target : staticSpies) {
        target.setFromScope(beanScope, null);
      }
      for (FieldTarget target : staticInjection) {
        target.setFromScope(beanScope, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private Object captorFor(Field field) {
    Class<?> type = field.getType();
    if (!ArgumentCaptor.class.isAssignableFrom(type)) {
      throw new IllegalStateException("@Captor field must be of the type ArgumentCaptor.\n Field: '" + field.getName() + "' has wrong type");
    }
    Class<?> cls = new GenericMaster().getGenericType(field);
    return ArgumentCaptor.forClass(cls);
  }

  void build(BeanScopeBuilder builder, Object testInstance) {
    if (testInstance != null) {
      buildForInstance(builder);
    } else {
      buildForStatics(builder);
    }
  }

  void buildForInstance(BeanScopeBuilder builder) {
    final BeanScopeBuilder.ForTesting forTesting = builder.forTesting();
    for (FieldTarget target : mocks) {
      forTesting.mock(target.type(), target.name());
    }
    for (FieldTarget target : spies) {
      forTesting.spy(target.type(), target.name());
    }
  }

  void buildForStatics(BeanScopeBuilder builder) {
    final BeanScopeBuilder.ForTesting forTesting = builder.forTesting();
    for (FieldTarget target : staticMocks) {
      forTesting.mock(target.type(), target.name());
    }
    for (FieldTarget target : staticSpies) {
      forTesting.spy(target.type(), target.name());
    }
  }

  void set(Field field, Object val, Object testInstance) throws IllegalAccessException {
    final MemberAccessor memberAccessor = Plugins.getMemberAccessor();
    memberAccessor.set(field, testInstance, val);
  }

  class FieldTarget {

    private final Field field;
    private final String name;
    private final boolean isStatic;

    FieldTarget(Field field, String name) {
      this.field = field;
      this.isStatic = Modifier.isStatic(field.getModifiers());
      this.name = name;
    }

    @Override
    public String toString() {
      return field.getName();
    }

    Class<?> type() {
      return field.getType();
    }

    String name() {
      return name;
    }

    boolean isStatic() {
      return isStatic;
    }

    void setFromScope(BeanScope beanScope, Object testInstance) throws IllegalAccessException {
      set(field, beanScope.get(type(), name), testInstance);
    }
  }

}
