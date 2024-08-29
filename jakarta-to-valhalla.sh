#!/usr/bin/env bash

## adjust code
find . -type f -name '*.java' -exec sed -i'' -e 's| /\*value\*/ class | value class |g' {} +

## required for the maven plugin to run (--enable-preview)
mv .mvn/jvm.config.disabled .mvn/jvm.config

## adjust poms
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|-Dnet.bytebuddy|--enable-preview -Dnet.bytebuddy|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- VALHALLA-START ___|<!-- VALHALLA-START -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|____ VALHALLA-END -->|<!-- VALHALLA-END -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- Javadoc-No-Preview -->|<additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->|g' {} +



sed -i 's|//@Disabled|@Disabled|g'  ./inject-generator/src/test/java/io/avaje/inject/generator/InjectProcessorTest.java


