# JUnit Version Compatibility

## Overview

The `weld-junit-jupiter` module **supports both JUnit Jupiter 5.x and 6.x** through a `provided` scope dependency strategy.

## How It Works

### Dependency Scope Strategy

JUnit dependencies in `weld-junit-jupiter` are declared with `provided` scope:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>provided</scope>
</dependency>
```

This means:
- **weld-junit-jupiter does NOT bundle JUnit** - users must provide their own JUnit version
- Users can choose to use **JUnit Jupiter 5.x OR 6.x** based on their project needs
- No version conflicts with user's own JUnit dependency

### Supported Versions

- ✅ **JUnit Jupiter 5.0+** through **6.x**
- ✅ **JUnit Platform 5.0+** through **6.x**

### User Configuration

Users simply add `weld-junit-jupiter` and their preferred JUnit version:

```xml
<!-- Add weld-junit-jupiter -->
<dependency>
    <groupId>org.jboss.weld</groupId>
    <artifactId>weld-junit-jupiter</artifactId>
    <version>6.0.0</version>
    <scope>test</scope>
</dependency>

<!-- Choose your JUnit version (5.x or 6.x) -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>6.0.0</version> <!-- or 5.13.4, or any 5.x/6.x version -->
    <scope>test</scope>
</dependency>
```

## Testing Strategy

The weld-testing project is tested against **both JUnit 5.x and 6.x**:
- **JUnit 6.0.0** (primary/default): 140/140 tests passing ✅
- **JUnit 5.13.4** (backward compatibility): 140/140 tests passing ✅

### Running Tests with Different JUnit Versions

To test with JUnit 5.x:
```bash
mvn test -Pjunit5
```

To test with JUnit 6.x (default):
```bash
mvn test -Pjunit6
# or simply:
mvn test
```

### Maven Profiles

The project includes two Maven profiles for dual version testing:

- **`junit6`** (active by default): Tests with JUnit Jupiter 6.0.0 and JUnit Platform 6.0.0
- **`junit5`**: Tests with JUnit Jupiter 5.13.4 and JUnit Platform 1.12.2

## Migration from JUnit 5 to JUnit 6

Users can migrate from JUnit 5 to JUnit 6 independently of upgrading weld-testing:

1. Upgrade to weld-testing 6.0.0 while staying on JUnit 5.x
2. Later, upgrade to JUnit 6.x when ready

OR

1. Upgrade to JUnit 6.x first
2. Then upgrade to weld-testing 6.0.0

Both paths are supported!

## API Compatibility

The weld-junit-jupiter module:
- Uses only stable JUnit APIs available in both 5.x and 6.x
- Avoids deprecated APIs that were removed in JUnit 6
- Has been updated for JUnit 6 API changes:
  - `MethodOrderer.Alphanumeric` → `MethodOrderer.MethodName`
  - `CollectionUtils.toUnmodifiableList()` → `Stream.toList()`

## Version Support Policy

- **Minimum supported version**: JUnit Jupiter 5.0.0
- **Maximum supported version**: JUnit Jupiter 6.x (latest)
- **Java baseline**: Java 17+ (required by JUnit 6)

## Known Limitations

- The Spock module currently has compatibility issues with JUnit Platform 6.0.0 due to upstream changes
- This is a known issue being tracked in the Spock project
- Spock users should continue using JUnit Platform 5.x until Spock 2.5+ is released
