package io.avaje.inject.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.GenericMaster;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;

final class MetaReader {

  private final SetupMethods methodFinder;
  final List<Field> captors = new ArrayList<>();
  final List<FieldTarget> mocks = new ArrayList<>();
  final List<FieldTarget> spies = new ArrayList<>();
  final List<FieldTarget> injection = new ArrayList<>();
  final List<FieldTarget> staticMocks = new ArrayList<>();
  final List<FieldTarget> staticSpies = new ArrayList<>();
  final List<FieldTarget> staticInjection = new ArrayList<>();
  boolean classInjection;
  boolean instanceInjection;

  final Plugin plugin;
  boolean staticPlugin;
  boolean instancePlugin;

  MetaReader(Class<?> testClass, Plugin plugin) {
    this.plugin = plugin;
    final var hierarchy = typeHierarchy(testClass);
    this.methodFinder = new SetupMethods(hierarchy);
    for (Class<?> aTestClass : hierarchy) {
      for (Field field : aTestClass.getDeclaredFields()) {
        readField(field);
      }
    }
  }

  boolean hasMocksOrSpies(Object testInstance) {
    if (testInstance == null) {
      return hasStaticMocksOrSpies() || methodFinder.hasStaticMethods();
    } else {
      return hasInstanceMocksOrSpies(testInstance) || methodFinder.hasInstanceMethods();
    }
  }

  private boolean hasInstanceMocksOrSpies(Object testInstance) {
    return !mocks.isEmpty() || !spies.isEmpty() || hasInjectMock(injection, testInstance);
  }

  private boolean hasStaticMocksOrSpies() {
    return !staticMocks.isEmpty() || !staticSpies.isEmpty() || hasInjectMock(staticInjection, null);
  }

  private boolean hasInjectMock(List<FieldTarget> fields, Object testInstance) {
    for (FieldTarget target : fields) {
      Object existingValue = target.get(testInstance);
      if (existingValue != null) {
        // an assigned injection field is a mock
        return true;
      }
    }
    return false;
  }

  private static LinkedList<Class<?>> typeHierarchy(Class<?> testClass) {
    var hierarchy = new LinkedList<Class<?>>();
    var analyzedClass = testClass;
    while (analyzedClass != null && !analyzedClass.equals(Object.class)) {
      hierarchy.addFirst(analyzedClass);
      analyzedClass = analyzedClass.getSuperclass();
    }
    return hierarchy;
  }

  boolean hasClassInjection() {
    return classInjection || methodFinder.hasStaticMethods();
  }

  boolean hasInstanceInjection() {
    return instanceInjection || methodFinder.hasInstanceMethods();
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder().append(toStringAppend("mocks:", mocks));
    s.append(toStringAppend("spies:", spies));
    s.append(toStringAppend("inject:", injection));
    s.append(toStringAppend("captors:", captors));
    s.append(toStringAppend("staticMocks:", staticMocks));
    s.append(toStringAppend("staticSpies:", staticSpies));
    s.append(toStringAppend("staticInjection:", staticInjection));
    return s.toString();
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
      FieldTarget target = newTarget(field);
      if (plugin != null && plugin.forType(target.type())) {
        target.markForPluginInjection();
        if (target.isStatic()) {
          staticPlugin = true;
        } else {
          instancePlugin = true;
        }
      }
      add(target, injection, staticInjection);
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
      return named.value();
    }
    for (Annotation annotation : field.getAnnotations()) {
      final var annotationType = annotation.annotationType();
      for (Annotation metaAnnotation : annotationType.getAnnotations()) {
        if (metaAnnotation.annotationType().equals(Qualifier.class)) {
          return AnnotationReader.simplifyAnnotation(annotation.toString())
            .replaceFirst(annotationType.getCanonicalName(), annotationType.getSimpleName())
            .replace("()", "").substring(1);
        }
      }
    }
    return null;
  }

  TestBeans setFromScope(TestBeans metaScope, Object testInstance) {
    if (testInstance != null) {
      return setForInstance(metaScope, testInstance);
    } else {
      return setForStatics(metaScope);
    }
  }

  private TestBeans setForInstance(TestBeans metaScope, Object testInstance) {
    try {
      Plugin.Scope pluginScope = metaScope.plugin();
      BeanScope beanScope = metaScope.beanScope();

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
        if (target.pluginInjection) {
          Object instance = pluginScope.create(target.type());
          target.setFromPlugin(instance, testInstance);
        } else {
          target.setFromScope(beanScope, testInstance);
        }
      }
      return metaScope;

    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private TestBeans setForStatics(TestBeans metaScope) {
    try {
      Plugin.Scope pluginScope = metaScope.plugin();
      BeanScope beanScope = metaScope.beanScope();
      for (FieldTarget target : staticMocks) {
        target.setFromScope(beanScope, null);
      }
      for (FieldTarget target : staticSpies) {
        target.setFromScope(beanScope, null);
      }
      for (FieldTarget target : staticInjection) {
        if (target.pluginInjection) {
          Object instance = pluginScope.create(target.type());
          target.setFromPlugin(instance, null);
        } else {
          target.setFromScope(beanScope, null);
        }
      }
      return metaScope;
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
      buildForInstance(builder, testInstance);
    } else {
      buildForStatics(builder);
    }
  }

  void buildForInstance(BeanScopeBuilder builder, Object testInstance) {
    for (FieldTarget target : mocks) {
      registerMock(testInstance, builder, target);
    }
    for (FieldTarget target : spies) {
      registerSpy(testInstance, builder, target);
    }
    for (FieldTarget target : injection) {
      Object existingValue = target.get(testInstance);
      if (existingValue != null) {
        registerAsTestDouble(builder, target, existingValue);
      }
    }
    methodFinder.invokeInstance(builder, testInstance);
  }

  void buildForStatics(BeanScopeBuilder builder) {
    for (FieldTarget target : staticMocks) {
      registerMock(null, builder, target);
    }
    for (FieldTarget target : staticSpies) {
      registerSpy(null, builder, target);
    }
    for (FieldTarget target : staticInjection) {
      Object existingValue = target.get(null);
      if (existingValue != null) {
        registerAsTestDouble(builder, target, existingValue);
      }
    }
    methodFinder.invokeStatics(builder);
  }

  private static void registerMock(Object testInstance, BeanScopeBuilder builder, FieldTarget target) {
    Object existingValue = target.get(testInstance);
    if (existingValue != null) {
      registerAsTestDouble(builder, target, existingValue);
    } else {
      builder.forTesting().mock(target.type(), target.name());
    }
  }

  private static void registerSpy(Object testInstance, BeanScopeBuilder builder, FieldTarget target) {
    Object existingValue = target.get(testInstance);
    if (existingValue != null) {
      registerAsTestDouble(builder, target, existingValue);
    } else {
      builder.forTesting().spy(target.type(), target.name());
    }
  }

  private static void registerAsTestDouble(BeanScopeBuilder builder, FieldTarget target, Object value) {
    target.markAsProvided();
    builder.bean(target.name(), target.type(), value);
  }

  void set(Field field, Object val, Object testInstance) throws IllegalAccessException {
    Plugins.getMemberAccessor().set(field, testInstance, val);
  }

  class FieldTarget {

    private final Field field;
    private final String name;
    private final boolean isStatic;
    private boolean pluginInjection;
    private boolean valueAlreadyProvided;

    FieldTarget(Field field, String name) {
      this.field = field;
      this.isStatic = Modifier.isStatic(field.getModifiers());
      this.name = name;
    }

    @Override
    public String toString() {
      return field.getName();
    }

    Type type() {
      return field.getGenericType();
    }

    String name() {
      return name;
    }

    boolean isStatic() {
      return isStatic;
    }

    Object get(Object instance) {
      try {
        return Plugins.getMemberAccessor().get(field, instance);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    void setFromScope(BeanScope beanScope, Object testInstance) throws IllegalAccessException {
      if (valueAlreadyProvided) {
        return;
      }
      final var type = type();

      if (type instanceof ParameterizedType) {
        final var parameterizedType = (ParameterizedType) type;
        final var rawType = parameterizedType.getRawType();
        final var typeArguments = parameterizedType.getActualTypeArguments();

        if (rawType.equals(List.class) || rawType.equals(Optional.class)) {
          set(field, beanScope.list(typeArguments[0]), testInstance);
          return;
        }
      }

      if (!valueAlreadyProvided) {
        set(field, beanScope.get(type, name), testInstance);
      }
    }

    void setFromPlugin(Object value, Object testInstance) throws IllegalAccessException {
      set(field, value, testInstance);
    }

    void markForPluginInjection() {
      pluginInjection = true;
    }

    void markAsProvided() {
      valueAlreadyProvided = true;
    }
  }

}
