# Valhalla Getting started
How to get started with Valhalla Early Access builds. Installing and using with Maven.

### References:
https://openjdk.org/projects/valhalla/early-access

### Download
https://jdk.java.net/valhalla/

### (Maybe) have a directory to put it into
```bash
mkdir -p ~/localjdk
mv ~/Downloads/openjdk-* ~/localjdk
```

### Extract the tarball
```bash
cd ~/localjdk
tar -xvf openjdk-*.tar.gz
```

### MacOS Only:
On macOS, after expanding the tar.gz archive, you may need to remove the quarantine attribute from the bits before commands can be executed.
```bash
xattr -d com.apple.quarantine ./jdk-23.jdk
```

### Rename the jdk directory to something more obvious
```bash
mv jdk-23.jdk valhalla-23.jdk
```

### SDK Man install (MacOS)
```bash
# the full path to the jdk is:
echo $(realpath valhalla-23.jdk/Contents/Home)

# install it to sdkman
sdk install java 23.ea.valhalla $(realpath valhalla-23.jdk/Contents/Home)

# check it ...
sdk use java 23.ea.valhalla
java -version
```

```
❯ java -version
openjdk version "23-valhalla" 2024-09-17
OpenJDK Runtime Environment (build 23-valhalla+1-90)
OpenJDK 64-Bit Server VM (build 23-valhalla+1-90, mixed mode, sharing)
```

### Cleanup (optional)
```bash
# remove the tarball
rm ~/localjdk/openjdk-23-valhalla+1-90_macos-aarch64_bin.tar.gz
```

### Uninstall
```bash
# uninstall it from sdkman
sdk uninstall java 23.ea.valhalla
```


# Maven

### enable preview features
Enable preview features in the compiler plugin, surefire, failsafe, and javadoc plugin.

#### surefire, failsafe, compiler plugin
```xml
  <properties>
    <!-- surefire and failsafe -->
    <argLine>--enable-preview</argLine>
    <!-- compiler plugin -->
    <maven.compiler.release>23</maven.compiler.release>
    <maven.compiler.enablePreview>true</maven.compiler.enablePreview>
  </properties>
```

#### maven-javadoc-plugin
```xml
 <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <configuration>
      <additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->
    </configuration>
</plugin>
```

#### Plugins?
If using maven plugins that run during the build these also might
need to enable preview features.

Create a file in the root of the project called `.mvn/jvm.config` and add the
following line into that file:
```bash
--enable-preview
```


# Using `value` classes

### Record
Most record types are good candidates for value classes.
```java
public value class MyRecord(String name, int age) {}
```

### Value Class
```java
value class MyValueClass {

  // All fields are final
  private /*final*/ OtherThing dependency;

  MyValueClass(OtherThing dependency) {
    this.dependency = dependency;
  }
  ...
}
```
### Restrictions
All fields are final and no "Identity" so:
- All fields are final
- No use of `synchronized`
- No use of `Object.wait()`
- No use of `Object.notify()`
- the == operator compares value class instances according to their field values, without regard to when or where they were created
