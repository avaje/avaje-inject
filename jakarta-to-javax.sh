#!/usr/bin/env bash

## adjust pom dependencies

sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' inject/pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' inject-generator/pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' inject-test/pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' blackbox-aspect/pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' blackbox-other/pom.xml
sed -i '0,/<version>[^<]*\(-SNAPSHOT\)\{0,1\}<\/version>/ s/<version>\([^<]*\)\(-SNAPSHOT\)\{0,1\}<\/version>/<version>\1-javax\2<\/version>/' blackbox-test-inject/pom.xml

sed -i'' -e 's|<version>2\.0\.1</version> <!-- jakarta -->|<version>1\.0\.5</version> <!-- javax -->|g' inject/pom.xml

## adjust module-info
sed -i'' -e 's| jakarta\.inject| java\.inject|g' inject/src/main/java/module-info.java

## adjust code
#find . -type f -name '*.java' -exec sed -i'' -e 's| jakarta\.inject\.| javax\.inject\.|g' {} +
find . -type f -not -name 'IncludeAnnotations.java' -name '*.java' -exec sed -i'' -e 's|jakarta\.inject\.|javax\.inject\.|g' {} +
