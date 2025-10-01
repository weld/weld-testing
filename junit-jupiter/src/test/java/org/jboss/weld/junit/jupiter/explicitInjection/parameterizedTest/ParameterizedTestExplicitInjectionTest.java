package org.jboss.weld.junit.jupiter.explicitInjection.parameterizedTest;

import java.util.Set;

import jakarta.enterprise.inject.Default;

import org.jboss.weld.junit.jupiter.WeldJUnitJupiterExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(WeldJUnitJupiterExtension.class)
public class ParameterizedTestExplicitInjectionTest {

    public static final Set<String> strings = Set.of("one", "two", "three");

    @ParameterizedTest
    @ValueSource(strings = { "one", "two", "three" })
    public void noWeldInjection(String param) {
        // String param is not resolved by Weld
        Assertions.assertTrue(strings.contains(param));
    }

    // NOTE: swapping parameters in the below tests leads to a failure! Parameterized test attempts to claim the first
    // parameter as its own and cast to given type; there is nothing we can do about that
    @ParameterizedTest
    @ValueSource(strings = { "one", "two", "three" })
    public void parameterizedTestWithWeldInjection(String param, @Default Foo foo) {
        // String param is not resolved by Weld
        Assertions.assertTrue(strings.contains(param));
        // Foo has explicit qualifier and Weld therefore still tried to resolve it
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(Foo.class.getSimpleName(), foo.ping());
    }

    @ParameterizedTest
    @ValueSource(strings = { "one", "two", "three" })
    public void parameterizedTestWithTwoStringParams(String param, @Default String anotherString) {
        // String param is not resolved by Weld
        Assertions.assertTrue(strings.contains(param));
        // Foo has explicit qualifier and Weld therefore still tried to resolve it
        Assertions.assertNotNull(anotherString);
        Assertions.assertEquals("fooString", anotherString);
    }
}
