package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.typeElement;
import static io.avaje.inject.generator.ProcessingContext.addImportedAspects;
import static io.avaje.inject.generator.ProcessingContext.delayedElements;
import static io.avaje.inject.generator.ProcessingContext.loadMetaInfCustom;
import static io.avaje.inject.generator.ProcessingContext.loadMetaInfServices;
import static io.avaje.inject.generator.ProcessingContext.processingOver;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.StandardLocation;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateUtils;

@GenerateUtils
@GenerateAPContext
@SupportedOptions({"mergeServices", "buildPlugin"})
@SupportedAnnotationTypes({
  AspectImportPrism.PRISM_TYPE,
  AssistFactoryPrism.PRISM_TYPE,
  ComponentPrism.PRISM_TYPE,
  Constants.TESTSCOPE,
  ControllerPrism.PRISM_TYPE,
  ExternalPrism.PRISM_TYPE,
  FactoryPrism.PRISM_TYPE,
  ImportPrism.PRISM_TYPE,
  InjectModulePrism.PRISM_TYPE,
  MainClassPrism.PRISM_TYPE,
  PluginProvidesPrism.PRISM_TYPE,
  PrototypePrism.PRISM_TYPE,
  QualifierPrism.PRISM_TYPE,
  ScopePrism.PRISM_TYPE,
  SingletonPrism.PRISM_TYPE,
  ServiceProviderPrism.PRISM_TYPE
})
public final class InjectProcessor extends AbstractProcessor {

  private Elements elementUtils;
  private ScopeInfo defaultScope;
  private AllScopes allScopes;
  private boolean readModuleInfo;
  private final Set<String> pluginFileProvided = new HashSet<>();
  private final Set<String> moduleFileProvided = new HashSet<>();
  private final List<ModuleData> moduleData = new ArrayList<>();
  private int rounds;
  private boolean wroteMainClass;
  private final Set<String> customBeanAnnotations = new HashSet<>();
  private final Set<String> declaredBeanAnnotations = new TreeSet<>();


  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    if (customBeanAnnotations.isEmpty()) {
      return super.getSupportedAnnotationTypes();
    }
    var types = new HashSet<>(super.getSupportedAnnotationTypes());
    types.addAll(customBeanAnnotations);
    return types;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APContext.init(processingEnv);
    loadProvidedFiles();
    loadExternalBeanAnnotations();
    ProcessingContext.registerProvidedTypes(moduleFileProvided);
    moduleData.forEach(ProcessingContext::addModule);
    this.elementUtils = processingEnv.getElementUtils();
    this.allScopes = ProcessingContext.allScopes();
    this.defaultScope = allScopes.defaultScope();
    ExternalProvider.registerPluginProvidedTypes(defaultScope);
    pluginFileProvided.forEach(defaultScope::pluginProvided);

    // write a note in target so that other apts can know inject is running
    try {
      var file = APContext.getBuildResource("avaje-processors.txt");
      var addition = new StringBuilder();
      //if file exists, dedup and append current processor
      if (file.toFile().exists()) {
        var result = Stream.concat(Files.lines(file), Stream.of("avaje-inject-generator"))
          .distinct()
          .collect(joining("\n"));
        addition.append(result);
      } else {
        addition.append("avaje-inject-generator");
      }
      Files.writeString(file, addition.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      PomPluginWriter.addPlugin2Pom();
    } catch (IOException e) {
      // not an issue worth failing over
    }
  }

  /**
   * Loads provider files generated by avaje-inject-maven-plugin
   */
  void loadProvidedFiles() {
    lines("avaje-module-dependencies.csv").stream()
      .filter(s -> s.contains("|") && !s.startsWith("External Module Type"))
      .distinct()
      .map(l -> l.split("\\|"))
      .map(ModuleData::of)
      .flatMap(Optional::stream)
      .forEach(m -> {
        ExternalProvider.registerExternalMetaData(m.name());
        ExternalProvider.readMetaDataProvides(moduleFileProvided);
        this.moduleData.add(m);
      });
    lines("avaje-plugins.csv").stream()
      .filter(s -> s.contains("|") && !s.startsWith("External Plugin Type"))
      .distinct()
      .map(l -> l.split("\\|")[1])
      .flatMap(provides -> ModuleData.splitTopLevel(provides).stream())
      .filter(not(String::isBlank))
      .forEach(pluginFileProvided::add);
  }

  private void loadExternalBeanAnnotations() {
    lines("avaje-bean-annotations.csv").stream()
      .map(String::trim)
      .filter(not(String::isBlank))
      .forEach(customBeanAnnotations::add);
    try {
      var resources = InjectProcessor.class.getClassLoader().getResources(Constants.META_INF_BEAN_ANNOTATIONS);
      while (resources.hasMoreElements()) {
        try (var reader = new BufferedReader(new InputStreamReader(resources.nextElement().openStream(), StandardCharsets.UTF_8))) {
          reader.lines()
            .map(String::trim)
            .filter(not(String::isBlank))
            .forEach(customBeanAnnotations::add);
        }
      }
    } catch (IOException e) {
    }
  }

  private void writeBeanAnnotations() {
    try (var file = APContext.filer().getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_BEAN_ANNOTATIONS).openReader(true);
         var reader = new BufferedReader(file)) {
      reader.lines()
        .map(String::trim)
        .filter(not(String::isBlank))
        .filter(name -> {
          var type = typeElement(name);
          return type != null && isCustomBeanAnnotation(type);
        })
        .forEach(declaredBeanAnnotations::add);
    } catch (Exception e) {
    }
    if (declaredBeanAnnotations.isEmpty()) {
      return;
    }
    try {
      var jfo = ProcessingContext.createMetaInfWriterFor(Constants.META_INF_BEAN_ANNOTATIONS);
      var writer = new Append(jfo.openWriter());
      for (var annotation : declaredBeanAnnotations) {
        writer.append(annotation).eol();
      }
      writer.close();
    } catch (IOException e) {
      logError("Failed to write %s %s", Constants.META_INF_BEAN_ANNOTATIONS, e.getMessage());
    }
  }

  private List<String> lines(String relativeName) {
    try {
      return Files.readAllLines(APContext.getBuildResource(relativeName));
    } catch (final Exception e) {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    var itsOver = roundEnv.processingOver();
    if (itsOver || processingOver() || roundEnv.errorRaised()) {
      if (itsOver) {
        writeBeanAnnotations();
        ProcessingContext.writeSPIServicesFile();
        ProcessingContext.validateModule();
        ProcessingContext.clear();
      }
      return false;
    }
    processingOver(rounds++ > 0);
    maybeElements(roundEnv, MainClassPrism.PRISM_TYPE).ifPresent(this::processMainClass);

    APContext.setProjectModuleElement(annotations, roundEnv);
    readModule(roundEnv);

    readBeans(delayedElements());
    addImportedAspects(importedAspects(roundEnv));
    maybeElements(roundEnv, QualifierPrism.PRISM_TYPE).stream()
      .flatMap(Set::stream)
      .flatMap(e -> ElementFilter.methodsIn(e.getEnclosedElements()).stream())
      .forEach(this::validateQualifier);

    maybeElements(roundEnv, ScopePrism.PRISM_TYPE).ifPresent(this::readScopes);
    maybeBeanElements(roundEnv, FactoryPrism.PRISM_TYPE).ifPresent(this::readFactories);

    if (defaultScope.includeSingleton()) {
    	maybeBeanElements(roundEnv, SingletonPrism.PRISM_TYPE).ifPresent(this::readBeans);
    }
    maybeBeanElements(roundEnv, ComponentPrism.PRISM_TYPE).ifPresent(this::readBeans);
    maybeBeanElements(roundEnv, PrototypePrism.PRISM_TYPE).ifPresent(this::readBeans);
    readCustomBeanAnnotations(roundEnv);

    readImported(importedElements(roundEnv));

    maybeBeanElements(roundEnv, ControllerPrism.PRISM_TYPE).ifPresent(this::readBeans);
    maybeBeanElements(roundEnv, AssistFactoryPrism.PRISM_TYPE).ifPresent(this::readAssisted);

    maybeElements(roundEnv, ServiceProviderPrism.PRISM_TYPE).ifPresent(this::registerSPI);
    maybeElements(roundEnv, PluginProvidesPrism.PRISM_TYPE).ifPresent(this::registerSPI);
    allScopes.readBeans(roundEnv);
    final var over = processingOver();
    defaultScope.write(over);
    allScopes.write(over);

    if (processingOver()) {
      ProcessingContext.writeNativeImageReflectConfig(defaultScope.modulePackage());

      final var order =
        new FactoryOrder(ProcessingContext.modules(), defaultScope.pluginProvided())
          .orderModules();

      if (ProcessingContext.strictWiring()) {
        try {
          new SimpleOrderWriter(order, defaultScope).write();
        } catch (IOException e) {
          logError("FilerException trying to write wiring order class %s", e.getMessage());
        }
      }
    }
    return false;
  }

  private void validateQualifier(ExecutableElement method) {
    var type = APContext.asTypeElement(method.getReturnType());
    if (type == null || type.getKind() != ElementKind.ANNOTATION_TYPE) {
      return;
    }

    var enclosedMethods = ElementFilter.methodsIn(type.getEnclosedElements());
    if (enclosedMethods.size() > 1) {
      APContext.logError(method, "Qualifier annotation members can only have a single attribute");
    }
    enclosedMethods.forEach(this::validateQualifier);
  }

  // Optional because these annotations are not guaranteed to exist
  private Optional<? extends Set<? extends Element>> maybeElements(RoundEnvironment round, String name) {
    return Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);
  }

  // reset processingOver flag if anything needs processing in this round
  private Optional<? extends Set<? extends Element>> maybeBeanElements(RoundEnvironment round, String name) {
    final var op = Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);

    // reset processingOver flag if anything needs processing in this round
    processingOver(processingOver() && op.filter(n -> !n.isEmpty()).isEmpty());
    return op;
  }

  /**
   * Read beans annotated with custom annotations that are themselves meta-annotated with
   * {@code @Singleton}, {@code @Component} or {@code @Prototype}.
   */
  private void readCustomBeanAnnotations(RoundEnvironment roundEnv) {
    // custom annotations declared in this compilation
    Stream.of(SingletonPrism.PRISM_TYPE, ComponentPrism.PRISM_TYPE, PrototypePrism.PRISM_TYPE)
        .flatMap(name -> maybeElements(roundEnv, name).stream())
        .flatMap(Set::stream)
        .filter(e -> e.getKind() == ElementKind.ANNOTATION_TYPE)
        .map(e -> ((TypeElement) e).getQualifiedName().toString())
        .forEach(name -> {
          customBeanAnnotations.add(name);
          declaredBeanAnnotations.add(name);
        });

    ElementFilter.typesIn(roundEnv.getRootElements()).forEach(this::registerCustomBeanAnnotations);

    for (var annotation : customBeanAnnotations) {
      var annotationType = typeElement(annotation);
      if (annotationType == null
          || !defaultScope.includeSingleton() && !isCustomComponentOrPrototype(annotationType)) {
        continue;
      }
      maybeBeanElements(roundEnv, annotation).ifPresent(this::readBeans);
    }
  }

  private void registerCustomBeanAnnotations(TypeElement type) {
    for (var mirror : type.getAnnotationMirrors()) {
      var annotationType = (TypeElement) mirror.getAnnotationType().asElement();
      if (isCustomBeanAnnotation(annotationType)) {
        customBeanAnnotations.add(annotationType.getQualifiedName().toString());
      }
    }
    ElementFilter.typesIn(type.getEnclosedElements()).forEach(this::registerCustomBeanAnnotations);
  }

  private static boolean isCustomBeanAnnotation(TypeElement annotationType) {
    return SingletonPrism.isPresent(annotationType) || isCustomComponentOrPrototype(annotationType);
  }

  private static boolean isCustomComponentOrPrototype(TypeElement annotationType) {
    return ComponentPrism.isPresent(annotationType) || PrototypePrism.isPresent(annotationType);
  }

  private Set<TypeElement> importedElements(RoundEnvironment roundEnv) {
    return maybeBeanElements(roundEnv, ImportPrism.PRISM_TYPE).stream()
      .flatMap(Set::stream)
      .flatMap(base -> {
        var p = ImportPrism.getInstanceOn(base);
        var kind = p.kind();
        return p.value().stream()
          .map(ProcessingContext::asElement)
          .filter(this::notAlreadyProvided)
          .map(e -> {
            ProcessingContext.addComponentImportPkg(
              e.getQualifiedName().toString(), base);
            return registerImportedKind(e, kind);
          });
      })
      .collect(Collectors.toSet());
  }

  private static TypeElement registerImportedKind(TypeElement e, String kind) {
    if (!"SINGLETON".equals(kind)) {
      ProcessingContext.addImportedKind(e, kind);
    }
    return e;
  }

  private boolean notAlreadyProvided(TypeElement e) {
    final String type = e.getQualifiedName().toString();
    return !moduleFileProvided.contains(type) && !pluginFileProvided.contains(type);
  }

  private Map<String, AspectImportPrism> importedAspects(RoundEnvironment roundEnv) {
    return maybeElements(roundEnv, AspectImportPrism.PRISM_TYPE).stream()
      .flatMap(Set::stream)
      .map(AspectImportPrism::getAllInstancesOn)
      .flatMap(List::stream)
      .collect(Collectors.toMap(p -> p.value().toString(), p -> p));
  }

  private void readScopes(Set<? extends Element> scopes) {
    for (final Element element : scopes) {
      if (element.getKind() == ElementKind.ANNOTATION_TYPE && element instanceof TypeElement) {
        final var type = (TypeElement) element;
        allScopes.addScopeAnnotation(type);
      }
    }
    addTestScope();
  }

  /**
   * Add built-in test scope for <code>@TestScope</code> if available.
   */
  private void addTestScope() {
    final var testScopeType = elementUtils.getTypeElement(Constants.TESTSCOPE);
    if (testScopeType != null) {
      allScopes.addScopeAnnotation(testScopeType);
    }
  }

  private void readFactories(Set<? extends Element> beans) {
    readChangedBeans(ElementFilter.typesIn(beans), true, false);
  }

  private void readAssisted(Set<? extends Element> beans) {
    ElementFilter.typesIn(beans).forEach(t -> {
      var reader = new AssistBeanReader(t);
      try {
        new SimpleAssistWriter(reader).write();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private void readBeans(Set<? extends Element> beans) {
    readChangedBeans(ElementFilter.typesIn(beans), false, false);
  }

  private void readImported(Set<? extends Element> beans) {
    readChangedBeans(ElementFilter.typesIn(beans), false, true);
  }

  /**
   * Read the beans that have changed.
   */
  private void readChangedBeans(Set<TypeElement> beans, boolean factory, boolean importedComponent) {
    for (final var typeElement : beans) {
      if (typeElement.getKind() == ElementKind.INTERFACE
          || typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
        continue;
      }
      if (factory && typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
        logError(typeElement, "@Factory Classes cannot be nested");
      }
      final var scope = findScope(typeElement);
      if (!factory) {
        // will be found via custom scope so effectively ignore additional @Singleton
        if (scope == null) {
          defaultScope.read(typeElement, false, importedComponent);
        }
      } else if (scope != null) {
        scope.read(typeElement, true, false);
      } else {
        defaultScope.read(typeElement, true, false);
      }
    }
  }

  /**
   * Find the scope if the Factory has a scope annotation.
   */
  private ScopeInfo findScope(Element element) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var scopeInfo = allScopes.get(annotationMirror.getAnnotationType().toString());
      if (scopeInfo != null) {
        return scopeInfo;
      }
    }
    return null;
  }

  /**
   * Read the existing meta-data from InjectModule (if found) and the factory bean (if exists).
   */
  private void readModule(RoundEnvironment roundEnv) {
    var moduleList =
      maybeElements(roundEnv, InjectModulePrism.PRISM_TYPE).stream()
        .flatMap(Set::stream)
        .filter(e -> !ScopePrism.isPresent(e))
        .filter(e -> !GeneratedPrism.isPresent(e))
        .collect(toList());

    if (moduleList.size() > 1) {
      for (var element : moduleList) {
        logError(element, "There should only be one default scope @InjectModule annotation in a module");
      }
    }

    if (readModuleInfo) {
      // only read the module meta data once
      return;
    }
    readModuleInfo = true;
    final var factory = loadMetaInfServices();
    if (factory != null) {
      final var moduleType = elementUtils.getTypeElement(factory);
      if (moduleType != null) {
        defaultScope.readModuleMetaData(moduleType);
      }
    }
    allScopes.readModules(loadMetaInfCustom());
    readInjectModule(roundEnv);
  }

  /** Read InjectModule for things like package-info etc (not for custom scopes) */
  private void readInjectModule(RoundEnvironment roundEnv) {
    // read other that are annotated with InjectModule
    maybeElements(roundEnv, InjectModulePrism.PRISM_TYPE).stream()
      .flatMap(Set::stream)
      .forEach(element -> {
        final var scope = ScopePrism.getInstanceOn(element);
        if (scope == null) {
          // it it not a custom scope annotation
          final var annotation = InjectModulePrism.getInstanceOn(element);
          if (annotation != null) {
            defaultScope.details(annotation.name(), element);
            final boolean interweave = annotation.interweave();
            ProcessingContext.interweave(interweave);
            ProcessingContext.strictWiring(interweave || annotation.strictWiring());
          }
        }
      });
  }

  private void registerSPI(Set<? extends Element> beans) {
    ElementFilter.typesIn(beans).stream()
      .filter(this::isExtension)
      .map(TypeElement::getQualifiedName)
      .map(Object::toString)
      .forEach(ProcessingContext::addInjectSPI);
  }

  private boolean isExtension(TypeElement te) {
    PluginProvidesPrism.getOptionalOn(te).ifPresent(t -> {
      if (!APContext.isAssignable(te, "io.avaje.inject.spi.InjectPlugin")) {
        APContext.logError(te, "PluginProvides can only be placed on io.avaje.inject.spi.InjectPlugin");
      }
    });
    ServiceProviderPrism.getOptionalOn(te).ifPresent(t -> {
      if (APContext.isAssignable(te, "io.avaje.inject.spi.InjectPlugin")) {
        APContext.logWarn(te, "PluginProvides should be used to auto register InjectPlugins");
      }
    });

    return APContext.isAssignable(te, "io.avaje.inject.spi.InjectExtension");
  }

  private void processMainClass(Set<? extends Element> elements) {
    if (wroteMainClass) {
      return;
    }
    var mainClasses = ElementFilter.typesIn(elements);
    if (mainClasses.isEmpty()) {
      return;
    }
    if (mainClasses.size() > 1) {
      for (var element : mainClasses) {
        logError(element, "Only one class should be annotated with @MainClass");
      }
      return;
    }

    var mainElement = mainClasses.iterator().next();
    if (!hasMainMethod(mainElement)) {
      logError(mainElement, "@MainClass annotated class must have a main method");
      return;
    }

    checkGradlePlugin(mainElement);
    wroteMainClass = true;
    final var prism = MainClassPrism.getInstanceOn(mainElement);
    ProcessingContext.strictWiring(prism.strictWiring());
    ProcessingContext.interweave(prism.interweave());

    var qualifiedName = mainElement.getQualifiedName().toString();
    try {
      PomMainClassWriter.writeMainClass(qualifiedName);
    } catch (IOException e) {
      // not an issue worth failing over
      APContext.logNote("Unable to update pom.xml with mainClass: %s", e.getMessage());
    }
  }

  private void checkGradlePlugin(TypeElement mainElement) {
    try {
      var buildResourcePath = APContext.getBuildResource("").getParent();
      var pomPath = buildResourcePath.resolve("pom.xml");
      if (!pomPath.toFile().exists()) {
        var pluginExists = buildResourcePath.resolve("avaje-plugins.csv").toFile().exists();
        if (!pluginExists) {
          logError(mainElement, "@MainClass requires the avaje-inject Gradle plugin - add 'io.avaje.inject' to your build.gradle plugins block");
        }
      }
    } catch (Exception e) {
      // unable to check, skip
    }
  }

  private boolean hasMainMethod(TypeElement typeElement) {
    return ElementFilter.methodsIn(typeElement.getEnclosedElements()).stream()
      .anyMatch(method ->
        "main".equals(method.getSimpleName().toString())
          && !method.getModifiers().contains(Modifier.PRIVATE)
          && method.getReturnType().getKind() == TypeKind.VOID
          && (method.getModifiers().contains(Modifier.STATIC)
          || APContext.jdkVersion() >= 25)
          && isValidMainParams(method));
  }

  private boolean isValidMainParams(ExecutableElement method) {
    var params = method.getParameters();
    return params.isEmpty() && APContext.jdkVersion() >= 25
        || (params.size() == 1 && params.get(0).asType().toString().contains("String[]"));
  }
}
