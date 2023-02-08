package io.avaje.inject.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
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

final class ProcessingContext {

  private final ProcessingEnvironment processingEnv;
  private final Messager messager;
  private final Filer filer;
  private final Elements elementUtils;
  private final Types typeUtils;
  private final Set<String> uniqueModuleNames = new HashSet<>();
  private final ExternalProvider externalProvide = new ExternalProvider();

  ProcessingContext(ProcessingEnvironment processingEnv, Set<String> moduleFileProvided) {
    this.processingEnv = processingEnv;
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
    externalProvide.init(moduleFileProvided);
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

  boolean externallyProvided(String type) {
    return externalProvide.provides(type);
  }

  /**
   * Return the name via <code>@Named</code> only.
   */
  static String named(Element p) {
    AnnotationMirror named = annotation(p, Constants.NAMED);
    if (named != null) {
      for (AnnotationValue value : named.getElementValues().values()) {
        return value.getValue().toString().toLowerCase();
      }
    }
    return null;
  }

  /**
   * Return the name via <code>@Named</code> or a Qualifier annotation.
   */
  public static String namedQualifier(Element p) {
    String named = named(p);
    if (named != null) {
      return named;
    }
    for (AnnotationMirror annotationMirror : p.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      AnnotationMirror qualifier = annotation(annotationType.asElement(), Constants.QUALIFIER);
      if (qualifier != null) {
        return Util.shortName(annotationType.toString()).toLowerCase();
      }
    }
    return null;
  }

  /**
   * Read the annotation attribute as a String value.
   */
  String readAttribute(AnnotationMirror annotation, String attrName) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
      if (attrName.equals(entry.getKey().getSimpleName().toString())) {
        return entry.getValue().getValue().toString();
      }
    }
    return "";
  }

  /**
   * Return the AnnotationMirror on the element for the given annotation type.
   */
  static AnnotationMirror annotation(Element element, String annotationType) {
    final List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
    for (AnnotationMirror mirror : mirrors) {
      final String name = mirror.getAnnotationType().asElement().toString();
      if (annotationType.equals(name)) {
        return mirror;
      }
    }
    return null;
  }

  /**
   * Return true if the element has the given annotation.
   */
  boolean hasAnnotation(Element element, String annotationType) {
    AnnotationMirror annotation = annotation(element, annotationType);
    return annotation != null;
  }

  TypeElement typeScope() {
    return elementUtils.getTypeElement(Constants.SCOPE);
  }

  TypeElement typeFactory() {
    return elementUtils.getTypeElement(Constants.FACTORY);
  }

  TypeElement typeSingleton() {
    return elementUtils.getTypeElement(Constants.SINGLETON);
  }

  TypeElement typeComponent() {
    return elementUtils.getTypeElement(Constants.COMPONENT);
  }

  TypeElement typePrototype() {
    return elementUtils.getTypeElement(Constants.PROTOTYPE);
  }

  TypeElement typeProxy() {
    return elementUtils.getTypeElement(Constants.PROXY);
  }

  TypeElement typeInjectModule() {
    return elementUtils.getTypeElement(Constants.INJECTMODULE);
  }
}
