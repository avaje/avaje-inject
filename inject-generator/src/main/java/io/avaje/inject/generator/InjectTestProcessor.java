package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.typeElement;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"io.avaje.inject.test.InjectTest"})
public final class InjectTestProcessor extends AbstractProcessor {

  private boolean wroteLookup;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!wroteLookup
        && !Optional.ofNullable(typeElement("io.avaje.inject.test.InjectTest"))
            .map(roundEnv::getElementsAnnotatedWith)
            .orElse(Set.of())
            .isEmpty()) {
      wroteLookup = true;
      writeLookup();
    }
    return false;
  }

  private void writeLookup() {
    var template =
        "package io.avaje.inject.test.lookup;\n"
            + "\n"
            + "import java.lang.invoke.MethodHandles;\n"
            + "import java.lang.invoke.MethodHandles.Lookup;\n"
            + "\n"
            + "import io.avaje.inject.test.LookupProvider;\n"
            + "\n"
            + "public class TestLookup implements LookupProvider {\n"
            + "\n"
            + "  @Override\n"
            + "  public Lookup provideLookup() {\n"
            + "    return MethodHandles.lookup();\n"
            + "  }\n"
            + "}";

    try (var writer =
            APContext.createSourceFile("io.avaje.inject.test.lookup.TestLookup").openWriter();
        var services =
            ProcessingContext.createMetaInfWriterFor(
                    "META-INF/services/io.avaje.inject.test.LookupProvider")
                .openWriter()) {
      writer.append(template);
      services.append("io.avaje.inject.test.lookup.TestLookup");
    } catch (IOException e) {
      APContext.logWarn("failed to write lookup");
    }
  }
}
