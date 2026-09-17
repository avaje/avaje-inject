## How to release to central

- Release the Jakarta version

```shell
mvn clean verify
mvn clean deploy -Pcentral
```

## release gradle plugin

- In `build.gradle` bump `version` and version for the `io.avaje:avaje-inject` dependency
- `cd inject-gradle-plugin`
- `./gradlew clean publishPlugin`

## release javax version

```shell
#back to top directory
cd ..

java Jakarta2Javax.java
mvn clean verify
mvn clean deploy -Pcentral
```
