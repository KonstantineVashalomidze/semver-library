# semver-library

A small, dependency-free library that parses and compares versions according to 
[Semantic Versioning 2.0.0](https://semver.org/).


## Features

* Parsing version formats with optional pre-release and build parts
* Comparing versions to each other via __SemVer__ precedence
* Immutability and  use as a __Map__ key or in a __Set__
* No dependencies

## Installation

Installation via JitPack:

[![](https://jitpack.io/v/KonstantineVashalomidze/semver-library.svg)](https://jitpack.io/#KonstantineVashalomidze/semver-library)

## Quickstart

```java
import com.github.konstantinevashalomidze.semver.*;
public class Main {
    public static void main(String[] args) {
        Version rc = Version.parse("1.0.0-rc.1");
        Version release = Version.parse("1.0.0");
        System.out.println(rc.compareTo(release) < 0); // True
        
        Version beta2 = Version.parse("1.0.0-beta.2");
        Version beta11 = Version.parse("1.0.0-beta.11");
        System.out.println(beta2.compareTo(beta11) < 0); // True

        System.out.println(Version.parse("1.0.0+a").equals(Version.parse("1.0.0+b"))); // True

        Version v = Version.parse("1.4.2-beta.1+exp.sha.5114f85");
        System.out.println(v.major() + " " + v.minor() + " " + v.patch());
        System.out.println(v.preRelease()); // beta.1
        System.out.println(v.build()); // exp.sha.5114f85

        try {
            Version.parse("1.04.2");
        } catch (SemverInvalidInputException e) {
            System.out.println("Rejected: " + e.getMessage().lines().findFirst().orElse("")); // Rejected: Invalid version number 1.04.2

        }
    }
}
```

Output:

```
true
true
true
1 4 2
beta.1
exp.sha.5114f85
Rejected: Invalid version number 1.04.2
```

## API Reference

### Creating Version
```java
Version.parse("1.0.0");                     // Plain version
Version.parse("1.0.0-alpha");               // With pre-release
Version.parse("1.0.0+334A5f");              // With build
Version.parse("1.0.0-alpha.1+beta.11");     // With pre-release and build
new Version(1, 0, 0, "", "");               // Plain version
new Version(1, 0, 0, "alpha", "");          // With pre-release
new Version(1, 0, 0, "", "334A5f");         // With build 
new Version(1, 0, 0, "alpha.1", "beta.11"); // With pre-release and build
```
### Reading the parts
```java
version.major();         // Returns major
version.minor();         // Returns minor
version.patch();         // Returns patch
version.preRelease();    // Returns pre-release
version.build();         // retunrs Build
```

### Comparing
```java
v1.compareTo(v2);                     // Returns negative if v1 < v2, 0 if v1 = v2 and positive if v1 > v2
v1.equals(v2);                        // true if v1 equals v2 via SemVer precedence, build is not considered
v1.hashCode();                        // Returns hash code ignores build
```

## License

MIT - see [LICENSE](LICENSE) for details.