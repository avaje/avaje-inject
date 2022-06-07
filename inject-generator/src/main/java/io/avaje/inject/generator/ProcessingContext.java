package io.avaje.inject.generator;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.*;

class ProcessingContext {

  private final ProcessingEnvironment processingEnv;
  private final Messager messager;
  private final Filer filer;
  private final Elements elementUtils;
  private final Types typeUtils;
  private final Set<String> uniqueModuleNames = new HashSet<>();

  ProcessingContext(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
  }

  /**
   * Log an error message.
   */
  void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  void logError(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  void logWarn(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  void logDebug(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  String loadMetaInfServices() {
    final List<String> lines = loadMetaInf(Constants.META_INF_MODULE);
    return lines.isEmpty() ? null : lines.get(0);
  }

  List<String> loadMetaInfCustom() {
    return loadMetaInf(Constants.META_INF_CUSTOM);
  }

  private List<String> loadMetaInf(String fullName) {
    try {
      FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", fullName);
      if (fileObject != null) {
        List<String> lines = new ArrayList<>();
        Reader reader = fileObject.openReader(true);
        LineNumberReader lineReader = new LineNumberReader(reader);
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

    } catch (FilerException e) {
      logDebug("FilerException reading services file");

    } catch (Exception e) {
      e.printStackTrace();
      logWarn("Error reading services file: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String cls) throws IOException {
    return filer.createSourceFile(cls);
  }

  FileObject createMetaInfWriter(ScopeInfo.Type scopeType) throws IOException {
    String serviceName = scopeType == ScopeInfo.Type.DEFAULT ? Constants.META_INF_MODULE : Constants.META_INF_TESTMODULE;
    return createMetaInfWriterFor(serviceName);
  }

  private FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  TypeElement element(String rawType) {
    return elementUtils.getTypeElement(rawType);
  }

  Types types() {
    return typeUtils;
  }

  TypeElement elementMaybe(String rawType) {
    if (rawType == null) {
      return null;
    } else {
      return elementUtils.getTypeElement(rawType);
    }
  }

  Element asElement(TypeMirror returnType) {
    return typeUtils.asElement(returnType);
  }

  void addModule(String moduleFullName) {
    if (moduleFullName != null) {
      uniqueModuleNames.add(moduleFullName);
    }
  }

  boolean isDuplicateModule(String moduleFullName) {
    return uniqueModuleNames.contains(moduleFullName);
  }

}
