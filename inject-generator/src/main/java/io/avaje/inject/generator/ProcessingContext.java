package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.elements;
import static io.avaje.inject.generator.APContext.filer;
import static io.avaje.inject.generator.APContext.getModuleInfoReader;
import static io.avaje.inject.generator.APContext.getProjectModuleElement;
import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.logNote;
import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.APContext.typeElement;
import static io.avaje.inject.generator.APContext.asTypeElement;
import static io.avaje.inject.generator.APContext.types;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

  private ProcessingContext() {}

  static final class Ctx {
    private final Set<String> uniqueModuleNames = new HashSet<>();
    private final Set<String> providedTypes = new HashSet<>();
    private final Set<String> optionalTypes = new LinkedHashSet<>();
    private final Map<String, AspectImportPrism> aspectImportPrisms = new HashMap<>();
    private final List<AvajeModule> avajeModules = new ArrayList<>();
    private boolean validated;
    private boolean strictWiring;
    private String injectFqn;
    private String orderFqn;

    public Ctx(Set<String> moduleFileProvided) {

      providedTypes.addAll(moduleFileProvided);
    }

    public void registerExternalModules() {
      ExternalProvider.registerModuleProvidedTypes(providedTypes);
    }

    public Ctx() {}
  }

  public static void init(ProcessingEnvironment processingEnv, Set<String> moduleFileProvided) {
    CTX.set(new Ctx(moduleFileProvided));
    CTX.get().registerExternalModules();
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

  static void setInjectModuleFQN(String fqn) {
    CTX.get().injectFqn = fqn;
  }

  public static void setOrderFQN(String fqn) {
    CTX.get().orderFqn = fqn;
  }

  static void validateModule() {
    var module = getProjectModuleElement();
    if (module != null && !CTX.get().validated && !module.isUnnamed()) {

      CTX.get().validated = true;

      try (var reader = getModuleInfoReader()) {
        var injectFQN = CTX.get().injectFqn;
        var orderFQN = CTX.get().orderFqn;
        var providers = new ModuleInfoReader(module, reader).provides();
        var noProvides =
            injectFQN != null
                && providers.stream().noneMatch(s -> s.implementations().contains(injectFQN));
        var noProvidesOrder =
            orderFQN != null
                && providers.stream().noneMatch(s -> s.implementations().contains(orderFQN));

        if (noProvides) {
          logError(module, "Missing \"provides io.avaje.inject.spi.Module with %s;\"", injectFQN);
        }

        if (noProvidesOrder) {
          logError(
              module, "Missing \"provides io.avaje.inject.spi.ModuleOrdering with %s;\"", orderFQN);
        }

      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static Optional<AspectImportPrism> getImportedAspect(String type) {
    return Optional.ofNullable(CTX.get().aspectImportPrisms.get(type));
  }

  public static void clear() {
    CTX.remove();
    APContext.clear();
  }

  public static void addAvajeModule(AvajeModule module) {
    CTX.get().avajeModules.add(module);
  }

  public static List<AvajeModule> avajeModules() {
    return CTX.get().avajeModules;
  }

  public static void strictWiring(boolean strictWiring) {
    CTX.get().strictWiring = strictWiring;
  }

  public static boolean strictWiring() {
    return CTX.get().strictWiring;
  }
}
