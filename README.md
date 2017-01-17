# Weld JUnit Extensions

```xml
<dependency>
  <groupId>org.jboss.weld</groupId>
  <artifactId>weld-junit</artifactId>
  <version>${version.weld-junit}</version>
</dependency>
```

## WeldInitiator

`WeldInitiator` is a JUnit `TestRule`  which allows to start a Weld container per test method execution.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance.

```java
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

public class SimpleTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(Foo.class);

    @Test
    public void testFoo() {
        // Note that Weld container is automatically started
        // WeldInitiator can be used to perform programmatic lookup of beans
        assertEquals("baz", weld.select(Foo.class).get().getBaz());
    }

}
```