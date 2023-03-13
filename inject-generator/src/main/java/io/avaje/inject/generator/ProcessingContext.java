package io.avaje.inject.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final ThreadLocal<ProcessingEnvironment> ENV = new ThreadLocal<>();
  private static final ThreadLocal<Messager> MESSAGER = new ThreadLocal<>();
  private static final ThreadLocal<Filer> FILER = new ThreadLocal<>();
  private static final ThreadLocal<Elements> ELEMENT_UTILS = new ThreadLocal<>();
  private static final ThreadLocal<Types> TYPE_UTILS = new ThreadLocal<>();
  private static final ThreadLocal<Set<String>> UNIQUE_MODULE_NAMES =
      ThreadLocal.withInitial(HashSet::new);
  private static final ThreadLocal<Set<String>> PROVIDED_TYPES =
      ThreadLocal.withInitial(HashSet::new);
  private static final ThreadLocal<Set<String>> OPTIONAL_TYPES =
      ThreadLocal.withInitial(LinkedHashSet::new);

  private ProcessingContext() {}

  public static void init(ProcessingEnvironment processingEnv, Set<String> moduleFileProvided) {
    ENV.set(processingEnv);
    MESSAGER.set(processingEnv.getMessager());
    FILER.set(processingEnv.getFiler());
    ELEMENT_UTILS.set(processingEnv.getElementUtils());
    TYPE_UTILS.set(processingEnv.getTypeUtils());
    final var provided = PROVIDED_TYPES.get();
    ExternalProvider.registerModuleProvidedTypes(provided);
    provided.addAll(moduleFileProvided);
  }

  /** Log an error message. */
  static void logError(Element e, String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  static void logError(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  static void logWarn(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  static void logDebug(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
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
      final var fileObject = FILER.get().getResource(StandardLocation.CLASS_OUTPUT, "", fullName);
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
      logDebug("FilerException reading services file");

    } catch (final Exception e) {
      e.printStackTrace();
      logWarn("Error reading services file: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  /** Create a file writer for the given class name. */
  static JavaFileObject createWriter(String cls) throws IOException {
    return FILER.get().createSourceFile(cls);
  }

  static FileObject createMetaInfWriter(ScopeInfo.Type scopeType) throws IOException {
    final var serviceName =
        scopeType == ScopeInfo.Type.DEFAULT
            ? Constants.META_INF_MODULE
            : Constants.META_INF_TESTMODULE;
    return createMetaInfWriterFor(serviceName);
  }

  private static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return FILER.get().createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static TypeElement element(String rawType) {
    return ELEMENT_UTILS.get().getTypeElement(rawType);
  }

  static Types types() {
    return TYPE_UTILS.get();
  }

  static TypeElement elementMaybe(String rawType) {
    if (rawType == null) {
      return null;
    } else {
      return ELEMENT_UTILS.get().getTypeElement(rawType);
    }
  }

  static Element asElement(TypeMirror returnType) {

    final var wrapper = PrimitiveUtil.wrap(returnType.toString());

    return wrapper == null ? TYPE_UTILS.get().asElement(returnType) : element(wrapper);
  }

  static void addModule(String moduleFullName) {
    if (moduleFullName != null) {
      UNIQUE_MODULE_NAMES.get().add(moduleFullName);
    }
  }

  static boolean isDuplicateModule(String moduleFullName) {
    return UNIQUE_MODULE_NAMES.get().contains(moduleFullName);
  }

  static boolean externallyProvided(String type) {
    return PROVIDED_TYPES.get().contains(type) || OPTIONAL_TYPES.get().contains(type);
  }

  static void addOptionalType(String paramType) {
    if (!PROVIDED_TYPES.get().contains(paramType)) {
      OPTIONAL_TYPES.get().add(paramType);
    }
  }

  public static void clear() {
    ENV.remove();
    MESSAGER.remove();
    FILER.remove();
    ELEMENT_UTILS.remove();
    TYPE_UTILS.remove();
    UNIQUE_MODULE_NAMES.remove();
    PROVIDED_TYPES.remove();
    OPTIONAL_TYPES.remove();
  }
}
