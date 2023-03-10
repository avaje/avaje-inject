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

  private ProcessingContext() {}

  private static ProcessingEnvironment processingEnv;
  private static Messager messager;
  private static Filer filer;
  private static Elements elementUtils;
  private static Types typeUtils;
  private static final Set<String> uniqueModuleNames = new HashSet<>();
  private static final Set<String> providedTypes = new HashSet<>();
  private static final Set<String> optionalTypes = new LinkedHashSet<>();

 static void init(ProcessingEnvironment env, Set<String> moduleFileProvided) {
    processingEnv = env;
    messager = processingEnv.getMessager();
    filer = processingEnv.getFiler();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    ExternalProvider.registerModuleProvidedTypes(providedTypes);
    providedTypes.addAll(moduleFileProvided);
  }

  /**
   * Log an error message.
   */
 static void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

 static void logError(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

 static void logWarn(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

 static void logDebug(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
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
      final var fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", fullName);
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

  /**
   * Create a file writer for the given class name.
   */
 static JavaFileObject createWriter(String cls) throws IOException {
    return filer.createSourceFile(cls);
  }

 static FileObject createMetaInfWriter(ScopeInfo.Type scopeType) throws IOException {
    final var serviceName = scopeType == ScopeInfo.Type.DEFAULT ? Constants.META_INF_MODULE : Constants.META_INF_TESTMODULE;
    return createMetaInfWriterFor(serviceName);
  }

 private static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

 static TypeElement element(String rawType) {
    return elementUtils.getTypeElement(rawType);
  }

 static Types types() {
    return typeUtils;
  }

 static TypeElement elementMaybe(String rawType) {
    if (rawType == null) {
      return null;
    } else {
      return elementUtils.getTypeElement(rawType);
    }
  }

 static Element asElement(TypeMirror returnType) {

    var wrapper = PrimitiveUtil.wrap(returnType.toString());

    return wrapper == null ? typeUtils.asElement(returnType) : element(wrapper);
  }

 static void addModule(String moduleFullName) {
    if (moduleFullName != null) {
      uniqueModuleNames.add(moduleFullName);
    }
  }

 static boolean isDuplicateModule(String moduleFullName) {
    return uniqueModuleNames.contains(moduleFullName);
  }

 static boolean externallyProvided(String type) {
    return providedTypes.contains(type) || optionalTypes.contains(type);
  }

 static void addOptionalType(String paramType) {
    if (!providedTypes.contains(paramType)) {
      optionalTypes.add(paramType);
    }
  }
}
