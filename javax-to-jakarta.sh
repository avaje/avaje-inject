#!/usr/bin/env bash

## adjust pom dependencies

sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' inject/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' inject-aop/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' inject-generator/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' inject-test/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' blackbox-aspect/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' blackbox-other/pom.xml
sed -i 's/\(<version>[^<]*\)-javax\([^<]*<\/version>\)/\1\2/' blackbox-test-inject/pom.xml
sed -i '' -e 's|<version>1\.0\.5</version> <!-- javax -->|<version>2\.0\.1</version> <!-- jakarta -->|g' inject/pom.xml

## adjust module-info
sed -i '' -e 's| java\.inject| jakarta\.inject|g' inject/src/main/java/module-info.java

## adjust code
find . -type f -not -name 'IncludeAnnotations.java' -name '*.java' -exec sed -i '' -e 's|javax\.inject\.|jakarta\.inject\.|g' {} +

## Linux version

sed -i'' -e 's|<version>1\.0\.5</version> <!-- javax -->|<version>2\.0\.1</version> <!-- jakarta -->|g' inject/pom.xml

## adjust module-info
sed -i'' -e 's| java\.inject| jakarta\.inject|g' inject/src/main/java/module-info.java

## adjust code
find . -type f -not -name 'IncludeAnnotations.java' -name '*.java' -exec sed -i'' -e 's|javax\.inject\.|jakarta\.inject\.|g' {} +
