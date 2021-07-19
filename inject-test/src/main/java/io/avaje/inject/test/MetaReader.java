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
import java.util.ArrayList;
import java.util.List;

class MetaReader {

  private final List<Field> captors = new ArrayList<>();
  private final List<FieldTarget> mocks = new ArrayList<>();
  private final List<FieldTarget> spies = new ArrayList<>();
  private final List<FieldTarget> injection = new ArrayList<>();
  private final Object testInstance;

  MetaReader(Object testInstance) {
    this.testInstance = testInstance;
    final Class<?> cls = testInstance.getClass();
    for (Field field : cls.getDeclaredFields()) {
      readField(field);
    }
  }

  List<FieldTarget> mocks() {
    return mocks;
  }

  private void readField(Field field) {
    final Mock mockAnnotation = field.getAnnotation(Mock.class);
    if (mockAnnotation != null) {
      mocks.add(newTarget(field));
      return;
    }
    final Spy spyAnnotation = field.getAnnotation(Spy.class);
    if (spyAnnotation != null) {
      spies.add(newTarget(field));
      return;
    }
    final Captor captorAnnotation = field.getAnnotation(Captor.class);
    if (captorAnnotation != null) {
      captors.add(field);
      return;
    }
    final Inject injectAnnotation = field.getAnnotation(Inject.class);
    if (injectAnnotation != null) {
      injection.add(newTarget(field));
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

  void setFromScope(BeanScope beanScope) {
    try {
      for (Field field : captors) {
        set(field, captorFor(field));
      }
      for (FieldTarget mock : mocks) {
        mock.setFromScope(beanScope);
      }
      for (FieldTarget target : spies) {
        target.setFromScope(beanScope);
      }
      for (FieldTarget target : injection) {
        target.setFromScope(beanScope);
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

  void build(BeanScopeBuilder builder) {
    final BeanScopeBuilder.ForTesting forTesting = builder.forTesting();
    for (FieldTarget target : mocks) {
      forTesting.withMock(target.type(), target.name());
    }
    for (FieldTarget target : spies) {
      forTesting.withSpy(target.type(), target.name());
    }
  }

  void set(Field field, Object val) throws IllegalAccessException {
    final MemberAccessor memberAccessor = Plugins.getMemberAccessor();
    memberAccessor.set(field, testInstance, val);
  }

  class FieldTarget {

    private final Field field;
    private final String name;

    FieldTarget(Field field, String name) {
      this.field = field;
      this.name = name;
    }
    Class<?> type() {
      return field.getType();
    }
    String name() {
      return name;
    }

    void setFromScope(BeanScope beanScope) throws IllegalAccessException {
      set(field, beanScope.get(type(), name));
    }
  }

}
