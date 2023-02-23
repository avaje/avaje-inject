#!/usr/bin/env bash

## adjust pom dependencies
sed -i'' -e 's|<version>1.0.5</version> <!-- javax -->|<version>2.0.1</version> <!-- jakarta -->|g' inject/pom.xml

## adjust module-info
sed -i'' -e 's| java.inject;| jakarta.inject;|g' inject/src/main/java/module-info.java

## adjust code
find . -type f -name '*.java' -exec sed -i'' -e 's|jakarta.inject.|javax.inject.|g' {} +
