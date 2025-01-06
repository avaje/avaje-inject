package io.avaje.inject.generator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

  private static final String EVENTS_SPI = "io.avaje.inject.events.spi.ObserverManagerPlugin";
  private static final ThreadLocal<Ctx> CTX = ThreadLocal.withInitial(Ctx::new);
  private static boolean processingOver;
  private ProcessingContext() {}

  static final class Ctx {
    private final Set<String> uniqueModuleNames = new HashSet<>();
    private final Set<String> providedTypes = new HashSet<>();
    private final Map<String, String> importedProtoTypes = new HashMap<>();
    private final Set<String> optionalTypes = new LinkedHashSet<>();
    private final Map<String, AspectImportPrism> aspectImportPrisms = new HashMap<>();
    private final List<ModuleData> modules = new ArrayList<>();
    private final List<TypeElement> delayQueue = new ArrayList<>();
    private final Set<String> spiServices = new TreeSet<>();
    private final Set<String> externalSpi = new TreeSet<>();
    private boolean strictWiring;
    private final boolean mergeServices = APContext.getOption("mergeServices").map(Boolean::valueOf).orElse(true);

    void registerProvidedTypes(Set<String> moduleFileProvided) {
      ExternalProvider.registerModuleProvidedTypes(providedTypes);
      providedTypes.addAll(moduleFileProvided);
    }
  }

  static void registerProvidedTypes(Set<String> moduleFileProvided) {
    CTX.get().registerProvidedTypes(moduleFileProvided);
    addEventSPI();
  }

  private static void addEventSPI() {
    try {
      if (typeElement(EVENTS_SPI) != null || Class.forName(EVENTS_SPI) != null) {
        addExternalInjectSPI(EVENTS_SPI);
      }
    } catch (final ClassNotFoundException e) {
      // nothing
    }
  }

  static String loadMetaInfServices() {
    return loadMetaInf(Constants.META_INF_SPI).stream()
        .filter(ProcessingContext::isInjectModule)
        .findFirst()
        .orElse(null);
  }

  private static boolean isInjectModule(String spi) {
    var moduleType = APContext.typeElement(spi);
    return moduleType != null && moduleType.getInterfaces().stream()
      .map(TypeMirror::toString)
      .anyMatch(s -> s.contains("AvajeModule"));
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
      logWarn("Error reading services file: %s", e.getMessage());
    }
    return Collections.emptyList();
  }

  static void addInjectSPI(String type) {
    CTX.get().spiServices.add(type);
  }

  static void addExternalInjectSPI(String type) {
    if (CTX.get().mergeServices) {
      CTX.get().externalSpi.add(type);
    }
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

  static void addOptionalType(String paramType, String name) {
    if (!CTX.get().providedTypes.contains(paramType)) {
      CTX.get().optionalTypes.add(ProcessorUtils.trimAnnotations(Util.addQualifierSuffixTrim(name, paramType)));
    }
  }

  static void addImportedKind(TypeElement element, String kind) {
    CTX.get().importedProtoTypes.put(element.getQualifiedName().toString(), kind);
  }

  static boolean isImportedPrototype(TypeElement element) {
    return "prototype".equalsIgnoreCase(importedTypeKind(element));
  }

  static boolean isImportedLazy(TypeElement element) {
    return "lazy".equalsIgnoreCase(importedTypeKind(element));
  }

  private static String importedTypeKind(TypeElement element) {
    return CTX.get().importedProtoTypes.get(element.getQualifiedName().toString());
  }

  static void addImportedAspects(Map<String, AspectImportPrism> importedMap) {
    CTX.get().aspectImportPrisms.putAll(importedMap);
  }

  static void validateModule() {
    APContext.moduleInfoReader()
        .ifPresent(
            reader ->
                reader.validateServices(
                    "io.avaje.inject.spi.InjectExtension", CTX.get().spiServices));
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

  static void addModule(ModuleData module) {
    CTX.get().modules.add(module);
  }

  static List<ModuleData> modules() {
    return CTX.get().modules;
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

  static void writeSPIServicesFile() {
    readExistingMetaInfServices();
    if (CTX.get().spiServices.isEmpty()) {
      // no services to register
      return;
    }
    try {
      FileObject jfo = createMetaInfWriterFor(Constants.META_INF_SPI);
      if (jfo != null) {
        var writer = new Append(jfo.openWriter());
        CTX.get().externalSpi.addAll(CTX.get().spiServices);
        for (var service : CTX.get().externalSpi) {
          writer.append(service).eol();
        }
        writer.close();
      }
    } catch (IOException e) {
      logError("Failed to write services file %s", e.getMessage());
    }
  }

  private static void readExistingMetaInfServices() {
    try (final var file =
           APContext.filer()
             .getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_SPI)
             .toUri()
             .toURL()
             .openStream();
         final var buffer = new BufferedReader(new InputStreamReader(file));) {

      String line;
      while ((line = buffer.readLine()) != null) {
        line.replaceAll("\\s", "")
          .replace(",", "\n")
          .lines()
          .forEach(ProcessingContext::addExternalInjectSPI);
      }
    } catch (Exception e) {
      // not a critical error
    }
  }

  static void registerExternalProvidedTypes(ScopeInfo scopeInfo) {
    ExternalProvider.scanAllInjectPlugins(scopeInfo);
    ExternalProvider.scanAllAvajeModules(CTX.get().providedTypes);
  }
}
