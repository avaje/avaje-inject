package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.asTypeElement;
import static io.avaje.inject.generator.APContext.elements;
import static io.avaje.inject.generator.APContext.filer;
import static io.avaje.inject.generator.APContext.getModuleInfoReader;
import static io.avaje.inject.generator.APContext.getProjectModuleElement;
import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.logNote;
import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.APContext.typeElement;
import static io.avaje.inject.generator.APContext.types;
import static java.util.stream.Collectors.toSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();
private static boolean processingOver;

  private ProcessingContext() {}

  static final class Ctx {
    private final Set<String> uniqueModuleNames = new HashSet<>();
    private final Set<String> providedTypes = new HashSet<>();
    private final Set<String> optionalTypes = new LinkedHashSet<>();
    private final Map<String, AspectImportPrism> aspectImportPrisms = new HashMap<>();
    private final List<TypeElement> delayQueue = new ArrayList<>();
    private final Map<String, TypeElement> unknownTypes = new HashMap<>();

    private boolean validated;

    public Ctx(ProcessingEnvironment processingEnv, Set<String> moduleFileProvided) {
      ExternalProvider.registerModuleProvidedTypes(providedTypes);
      providedTypes.addAll(moduleFileProvided);
    }

    public Ctx() {}
  }

  public static void init(ProcessingEnvironment processingEnv, Set<String> moduleFileProvided) {
    CTX.set(new Ctx(processingEnv, moduleFileProvided));
    APContext.init(processingEnv);
  }

  public static void testInit() {
    CTX.set(new Ctx());
  }

  static String loadMetaInfServices() {
    final var lines = loadMetaInf(Constants.META_INF_MODULE);
    return lines.isEmpty() ? null : lines.get(0);
  }

  static List<String> loadMetaInfCustom() {
    return loadMetaInf(Constants.META_INF_CUSTOM);
  }

  private static List<String> loadMetaInf(String fullName) {
    try {
      final var fileObject = filer().getResource(StandardLocation.CLASS_OUTPUT, "", fullName);
      if (fileObject != null) {
        final List<String> lines = new ArrayList<>();
        final var reader = fileObject.openReader(true);
        final var lineReader = new LineNumberReader(reader);
        String line;
        while ((line = lineReader.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty()) {
            lines.add(line);
          }
        }
        return lines;
      }

    } catch (FileNotFoundException | NoSuchFileException e) {
      // logDebug("no services file yet");
    } catch (final FilerException e) {
      logNote("FilerException reading services file");
    } catch (final Exception e) {
      e.printStackTrace();
      logWarn("Error reading services file: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  static FileObject createMetaInfWriter(ScopeInfo.Type scopeType) throws IOException {
    final var serviceName =
        scopeType == ScopeInfo.Type.DEFAULT
            ? Constants.META_INF_MODULE
            : Constants.META_INF_TESTMODULE;
    return createMetaInfWriterFor(serviceName);
  }

  private static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return filer().createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static TypeElement elementMaybe(String rawType) {
    if (rawType == null) {
      return null;
    } else {
      return elements().getTypeElement(rawType);
    }
  }

  static TypeElement asElement(TypeMirror returnType) {
    final var wrapper = PrimitiveUtil.wrap(returnType.toString());
    return wrapper == null ? asTypeElement(returnType) : typeElement(wrapper);
  }

  static boolean isUncheckedException(TypeMirror returnType) {
    final var runtime = typeElement("java.lang.RuntimeException").asType();
    return types().isSubtype(returnType, runtime);
  }

  static void addModule(String moduleFullName) {
    if (moduleFullName != null) {
      CTX.get().uniqueModuleNames.add(moduleFullName);
    }
  }

  static boolean isDuplicateModule(String moduleFullName) {
    return CTX.get().uniqueModuleNames.contains(moduleFullName);
  }

  static boolean externallyProvided(String type) {
    return CTX.get().providedTypes.contains(type) || CTX.get().optionalTypes.contains(type);
  }

  static void addOptionalType(String paramType) {
    if (!CTX.get().providedTypes.contains(paramType)) {
      CTX.get().optionalTypes.add(paramType);
    }
  }

  static void addImportedAspects(Map<String, AspectImportPrism> importedMap) {
    CTX.get().aspectImportPrisms.putAll(importedMap);
  }

  static void validateModule(String injectFQN) {
    var module = getProjectModuleElement();
    if (module != null && !CTX.get().validated && !module.isUnnamed()) {

      CTX.get().validated = true;

      try (var reader = getModuleInfoReader()) {

        var noProvides = reader.lines().noneMatch(s -> s.contains(injectFQN));

        if (noProvides) {
          logError(module, "Missing \"provides io.avaje.inject.spi.Module with %s;\"", injectFQN);
        }

      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static Optional<AspectImportPrism> getImportedAspect(String type) {
    return Optional.ofNullable(CTX.get().aspectImportPrisms.get(type));
  }

  static Set<TypeElement> delayedElements() {
    var set =
        CTX.get().delayQueue.stream()
            .map(t -> t.getQualifiedName().toString())
            .map(APContext::typeElement)
            .collect(toSet());
    CTX.get().delayQueue.clear();
    return set;
  }

  static boolean delayUntilNextRound(TypeElement element) {
    if (!processingOver) {
      CTX.get().delayQueue.add(element);
    }
    return !processingOver;
  }

  public static void clear() {
    CTX.remove();
    APContext.clear();
  }

  public static void processingOver(boolean over) {
    processingOver = over;
  }
}
