# Weld JUnit Extensions

```xml
<dependency>
  <groupId>org.jboss.weld</groupId>
  <artifactId>weld-junit</artifactId>
  <version>${version.weld-junit}</version>
</dependency>
```

## WeldInitiator

`WeldInitiator` is a JUnit `TestRule` (JUnit 4.9+) which allows to start a Weld container per test method execution.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance - see also `WeldInitiator.of(Weld)` static method.
A convenient static method `WeldInitiator.of(Class<?>...)` is also provided - in this case, the container is optimized for testing purposes (with automatic discovery and concurrent deployment disabled) and only the given bean classes are considered. 

```java
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

public class SimpleTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(Foo.class);

    @Test
    public void testFoo() {
        // Note that Weld container is started automatically
        // WeldInitiator can be used to perform programmatic lookup of beans
        assertEquals("baz", weld.select(Foo.class).get().getBaz());
    }

}
```