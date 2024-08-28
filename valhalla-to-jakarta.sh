#!/usr/bin/env bash

## adjust code
find . -type f -name '*.java' -exec sed -i'' -e 's| value class | /\*value\*/ class |g' {} +

## required for the maven plugin to run (--enable-preview)
mv .mvn/jvm.config .mvn/jvm.config.disabled

## adjust poms
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|--enable-preview -Dnet.bytebuddy|-Dnet.bytebuddy|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- VALHALLA-START -->|<!-- VALHALLA-START ___|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- VALHALLA-END -->|____ VALHALLA-END -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->|<!-- Javadoc-No-Preview -->|g' {} +
