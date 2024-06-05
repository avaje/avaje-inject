package io.avaje.inject.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.NoSuchFileException;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.util.*;

import static io.avaje.inject.generator.APContext.*;
import static java.util.stream.Collectors.toSet;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();
  private static boolean processingOver;
  private ProcessingContext() {}

  static final class Ctx {
    private final Set<String> uniqueModuleNames = new HashSet<>();
    private final Set<String> providedTypes = new HashSet<>();
    private final Set<String> optionalTypes = new LinkedHashSet<>();
    private final Map<String, AspectImportPrism> aspectImportPrisms = new HashMap<>();
    private final List<AvajeModuleData> avajeModules = new ArrayList<>();
    private final List<TypeElement> delayQueue = new ArrayList<>();
    private boolean strictWiring;

    Ctx() {}

    void registerProvidedTypes(Set<String> moduleFileProvided) {
      ExternalProvider.registerModuleProvidedTypes(providedTypes);
      providedTypes.addAll(moduleFileProvided);
    }
  }

  static void init(Set<String> moduleFileProvided, boolean performModuleValidation) {
    CTX.set(new Ctx());
    CTX.get().registerProvidedTypes(moduleFileProvided);
  }

  static void testInit() {
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

  static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
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

  static void clear() {
    CTX.remove();
    APContext.clear();
  }

  static void addAvajeModule(AvajeModuleData module) {
    CTX.get().avajeModules.add(module);
  }

  static List<AvajeModuleData> avajeModules() {
    return CTX.get().avajeModules;
  }

  static void strictWiring(boolean strictWiring) {
    CTX.get().strictWiring = strictWiring;
  }

  static boolean strictWiring() {
    return CTX.get().strictWiring;
  }

  static void processingOver(boolean over) {
    processingOver = over;
  }
}
