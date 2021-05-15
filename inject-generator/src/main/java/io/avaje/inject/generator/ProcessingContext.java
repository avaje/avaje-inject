package io.avaje.inject.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
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

class ProcessingContext {

  private final ProcessingEnvironment processingEnv;
  private final Messager messager;
  private final Filer filer;
  private final Elements elementUtils;
  private final Types typeUtils;

  private String contextName;
  private String[] contextProvides;
  private String[] contextDependsOn;
  private String contextPackage;
  private String metaInfServicesLine;

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
    if (metaInfServicesLine == null) {
      metaInfServicesLine = loadMetaInf();
    }
    return metaInfServicesLine;
  }

  private String loadMetaInf() {
    // logDebug("loading metaInfServicesLine ...");
    try {
      FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_FACTORY);
      if (fileObject != null) {
        Reader reader = fileObject.openReader(true);
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line = lineReader.readLine();
        if (line != null) {
          return line.trim();
        }
      }

    } catch (FileNotFoundException | NoSuchFileException e) {
      // logDebug("no services file yet");

    } catch (FilerException e) {
      logDebug("FilerException reading services file");

    } catch (Exception e) {
      e.printStackTrace();
      logWarn("Error reading services file: " + e.getMessage());
    }
    return null;
  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String cls) throws IOException {
    return filer.createSourceFile(cls);
  }

  /**
   * Create a file writer for the given class name.
   */
  FileObject createMetaInfWriter() throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_FACTORY);
  }

  void setContextDetails(String name, String[] provides, String[] dependsOn, Element contextElement) {
    this.contextName = name;
    this.contextProvides = provides;
    this.contextDependsOn = dependsOn;

    // determine the context package (that we put the $diFactory class into)
    PackageElement pkg = elementUtils.getPackageOf(contextElement);
    logDebug("using package from element " + pkg);
    this.contextPackage = (pkg == null) ? null : pkg.getQualifiedName().toString();
  }

  void deriveContextName(String factoryPackage) {
    if (contextName == null) {
      contextName = factoryPackage;
    }
  }

  String contextName() {
    return contextName;
  }

  String getContextPackage() {
    return contextPackage;
  }

  TypeElement element(String rawType) {
    return elementUtils.getTypeElement(rawType);
  }

  Element asElement(TypeMirror returnType) {
    return typeUtils.asElement(returnType);
  }

  void buildNewBuilder(Append writer) {
    writer.append("    this.name = \"%s\";", contextName).eol();
    writer.append("    this.provides = ", contextProvides);
    buildStringArray(writer, contextProvides, true);
    writer.append(";").eol();
    writer.append("    this.dependsOn = ", contextDependsOn);
    buildStringArray(writer, contextDependsOn, true);
    writer.append(";").eol();
  }

  void buildAtContextModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@ContextModule(name=\"%s\"", contextName);
    if (!isEmpty(contextProvides)) {
      writer.append(", provides=");
      buildStringArray(writer, contextProvides, false);
    }
    if (!isEmpty(contextDependsOn)) {
      writer.append(", dependsOn=");
      buildStringArray(writer, contextDependsOn, false);
    }
    writer.append(")").eol();
  }

  private boolean isEmpty(String[] strings) {
    return strings == null || strings.length == 0;
  }

  private void buildStringArray(Append writer, String[] values, boolean asArray) {

    if (isEmpty(values)) {
      writer.append("null");
    } else {
      if (asArray) {
        writer.append("new String[]");
      }
      writer.append("{");
      int c = 0;
      for (String value : values) {
        if (c++ > 0) {
          writer.append(",");
        }
        writer.append("\"").append(value).append("\"");
      }
      writer.append("}");
    }
  }
}
