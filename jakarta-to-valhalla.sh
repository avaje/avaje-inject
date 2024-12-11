#!/usr/bin/env bash

## adjust code
find . -type f -name '*.java' -exec sed -i'' -e 's| /\*value\*/ class | value class |g' {} +

## required for the maven plugin to run (--enable-preview)
mv .mvn/jvm.config.disabled .mvn/jvm.config

## adjust poms
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|-Dnet.bytebuddy|--enable-preview -Dnet.bytebuddy|g' {} +
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|<!-- default-build-start -->|<!-- default-build-start ___|g' {} +
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|<!-- default-build-end -->|____ default-build-end -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|<!-- valhalla-build-start ___|<!-- valhalla-build-start -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|____ valhalla-build-end -->|<!-- valhalla-build-end -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i'' -e 's|<!-- Javadoc-No-Preview -->|<additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->|g' {} +


find . -type f -name 'InjectProcessorTest.java' -exec sed -i'' -e 's|//@Disabled|@org.junit.jupiter.api.Disabled // Valhalla|g' {} +
## sed -i'' -e 's|//@Disabled|@org.junit.jupiter.api.Disabled // Valhalla|g' ./inject-generator/src/test/java/io/avaje/inject/generator/InjectProcessorTest.java

