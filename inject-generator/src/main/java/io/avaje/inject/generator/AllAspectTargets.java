package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

class AllAspectTargets {

  private final ProcessingContext context;
  private final Map<String, AspectTarget> map = new HashMap<>();

  AllAspectTargets(ProcessingContext context) {
    this.context = context;
  }

  AspectTarget findTarget(String target) {
    return map.computeIfAbsent(target, _target -> {
      TypeElement element = context.element(_target);
      if (element == null) {
        context.logError("Unable to find Aspect element "+_target);
        return null;
      }
      return readTarget(element, target);
    });
  }

  private AspectTarget readTarget(TypeElement element, String target) {
    return new AspectTarget(new AspectTargetReader(context, element, target));
  }
}
