package io.avaje.inject.generator;

import static java.util.stream.Collectors.joining;

final class ConditionalWriter {
  private final Append writer;
  private final BeanConditions conditions;

  private boolean first = true;

  public ConditionalWriter(Append writer, BeanConditions conditions) {
    this.writer = writer;
    this.conditions = conditions;
  }

  public void write() {

    if (conditions.isEmpty()) return;

    writer.append("    if (");

    if (!conditions.profiles.isEmpty()) {
      first = false;
      writer.append(
          "!builder.containsProfiles(java.util.List.of(\"%s\"))",
          conditions.profiles.stream().collect(joining(",")));
    }
    for (final var requireType : conditions.requireTypes) {
      prefix();
      writer.append("!builder.contains(%s.class)", Util.shortName(requireType));
    }
    for (final var missing : conditions.missingTypes) {
      prefix();
      writer.append("builder.contains(%s.class)", Util.shortName(missing));
    }
    for (final var qualifier : conditions.qualifierNames) {
      prefix();
      writer.append("!builder.containsQualifier(\"%s\")", qualifier);
    }
    for (final var props : conditions.containsProps) {
      prefix();
      writer.append("builder.property().missing(\"%s\")", props);
    }
    for (final var props : conditions.missingProps) {
      prefix();
      writer.append("builder.property().contains(\"%s\")", props);
    }
    for (final var props : conditions.propertyEquals.entrySet()) {
      prefix();
      writer.append(
          "builder.property().notEqualTo(\"%s\", \"%s\")", props.getKey(), props.getValue());
    }
    for (final var props : conditions.propertyNotEquals.entrySet()) {
      prefix();
      writer.append("builder.property().equalTo(\"%s\", \"%s\")", props.getKey(), props.getValue());
    }
    writer.append(") {").eol().append("      return;").eol().append("    }").eol().eol();
  }

  private void prefix() {
    if (first) {
      first = false;
    } else {
      writer.eol().append("      || ");
    }
  }
}
