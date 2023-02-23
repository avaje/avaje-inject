#!/usr/bin/env bash

## adjust pom dependencies
sed -i'' -e 's|<version>2.0.1</version> <!-- jakarta -->|<version>1.0.5</version> <!-- javax -->|g' inject/pom.xml

## adjust module-info
sed -i'' -e 's| jakarta.inject;| java.inject;|g' inject/src/main/java/module-info.java

## adjust code
find . -type f -name '*.java' -exec sed -i'' -e 's|javax.inject.|jakarta.inject.|g' {} +
