# Weld JUnit 5 Extension

This extension uses the extension mechanism introduced in JUnit 5.
Therefore, in order to use this extension in your test, you have to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)`.
It will automatically inject into all your `@Inject` fields in given test instance and start/stop Weld SE container based on your configuration.
Even if no configuration is provided, the default one will kick in.
Supports both test lifecycles - per method and per class.

Requires JUnit 5 and Java 8.

## Table of contents

* [Maven Artifact](#maven-artifact)
* [Basic Example](#basic-example)
* [WeldInitiator](#weldinitiator)
  * [Flat Deployment](#flat-deployment)
  * [Convenient Starting Points](#convenient-starting-points)
    * [Test class injection](#test-class-injection)
    * [Activating context for a normal scope](#activating-context-for-a-normal-scope)
    * [Adding mock beans](#adding-mock-beans)
* [Additional Configuration](#additional-configuration)
  * [Explicit Parameter Injection](#explicit-parameter-injection)

## Maven Artifact

```xml
<dependency>
  <groupId>org.jboss.weld</groupId>
  <artifactId>weld-junit5</artifactId>
  <version>${version.weld-junit}</version>
</dependency>
```

## Basic Example

The simplest way to use this extension is to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)`.
If you are in for shorter annotations, you can also use `@EnableWeld`.
In such case, Weld container will be started before each test is run and stopped afterwards.
This default behaviour includes:

* Bootstrapping Weld SE container with
  * Disabled discovery
  * Added test class package as source of beans
  * Disabled concurrent deployment
* Injecting into test instance, e.g. into all `@Inject` fields
* Injecting into method parameters of your test methods
  * In case the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in CDI container
  * If you wish to change this behaviour, please refer to [additional configuration section](#explicit-parameter-injection)
* Shutting down the container after test is done

And this is how you achieve it:

```java
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJunit5Extension.class)
class BasicUsageTest {

    @Inject
    MyBean myBean;

    @Test
    public void testFoo(MyOtherBean otherBean) {
      // Weld SE container is bootstrapped here and the injection points are resolved
    }
}
```

## WeldInitiator

`org.jboss.weld.junit5.WeldInitiator` is an entry point you will want to define if you wish to customize how we bootstrap Weld.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance.
By default, the container is optimized for testing purposes, i.e. with automatic discovery and concurrent deployment disabled (see also `WeldInitiator.createWeld()`).
However, it is possible to provide a customized `Weld` instance  - see also `WeldInitiator.of(Weld)` and `WeldInitiator.from(Weld)` methods.
`WeldInitiator` also implements `javax.enterprise.inject.Instance` and therefore might be used to perform programmatic lookup of bean instances.

`WeldInitiator` should be a public field annotated with `@org.jboss.weld.junit5.WeldSetup`.
From there you can use static methods:

```java
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WeldJunit5Extension.class)
class MyNewTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(Some.class);

    @Test
    public void testFoo() {...}
}
```

### Flat Deployment

Unlike when using [Arquillian Weld embedded container](https://github.com/arquillian/arquillian-container-weld), bean archive isolation is enabled by default.
This behaviour can be changed by setting a system property `org.jboss.weld.se.archive.isolation` to `false` or through the `Weld.property()` method.
In that case, Weld will use a _"flat"_ deployment structure - all bean classes share the same bean archive and all beans.xml descriptors are automatically merged into one.
Thus alternatives, interceptors and decorators selected/enabled for a bean archive will be enabled for the whole application.

### Convenient Starting Points

A convenient static method `WeldInitiator.of(Class<?>...)` is also provided - in this case, the container is optimized for testing purposes and only the given bean classes are considered.

```java

@ExtendWith(WeldJunit5Extension.class)
class SimpleTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(Foo.class);

    @Test
    public void testFoo() {
        // Note that Weld container is started automatically

        // WeldInitiator can be used to perform programmatic lookup of beans
        assertEquals("baz", weld.select(Foo.class).get().getBaz());

        // WeldInitiator can be used to fire a CDI event
        weld.event().select(Baz.class).fire(new Baz());
    }

}
```

It's also possible to use `WeldInitiator.ofTestPackage()` - the container is optimized for testing purposes and all the classes from the test class package are added automatically.

```java

@ExtendWith(WeldJunit5Extension.class)
class AnotherSimpleTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.ofTestPackage();

    @Test
    public void testFoo() {
        // Alpha comes from the same package as AnotherSimpleTest
        assertEquals(1, weld.select(Alpha.class).ping());
    }

}
```

Furthermore, `WeldInitiator.Builder` can be used to customize the final `WeldInitiator` instance, e.g. to *activate a context for a given normal scope* or to *inject the test class*.

#### Test class injection

Everytime `WeldJunit5Extension` processes your test instance, it will automatically resolve all `@Inject` fields as well as attempt to resolve any test method parameters, should they be injectable beans.

```java
@ExtendWith(WeldJunit5Extension.class)
class InjectTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Foo.class).build();

  // Gets injected before executing test
    @Inject
    @MyQualifier
    Foo foo;

    @Test
    public void testFoo(Foo fooAsParam) {
        assertEquals(42, foo.getValue());
        assertEquals(42, fooAsParam.getValue());
    }
}
```

#### Activating context for a normal scope

`WeldInitiator.Builder.activate(Class<? extends Annotation>...)` makes it possible to activate and deactivate contexts for the specified normal scopes for each test method execution:

```java
@ExtendWith(WeldJunit5Extension.class)
class ContextsActivatedTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Foo.class, Oof.class)
            .activate(RequestScoped.class, SessionScoped.class).build();

    @Test
    public void testFoo() {
        // Contexts for @RequestScoped and @SessionScoped are active!
        // Foo is @RequestScoped
        weld.select(Foo.class).get().doSomethingImportant();
        // Oof is @SessionScoped
        weld.select(Oof.class).get().doSomethingVeryImportant();
    }
}
```

#### Adding mock beans

Sometimes you might need to add a mock for a bean that cannot be part of the test deployment, e.g. the original bean implementation has dependencies which cannot be satisfied in the test environment.
Very often, it's an ideal use case for mocking libraries, ie. to create a bean instance with the desired behavior.
In this case, there are two options.
The first option is to add a [producer method](http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#producer_method) to the test class and add the test class to the deployment.
The test class will be recognized as a bean and therefore the producer will also be discovered.

```java
interface Bar {
  String ping();
}

class Foo {
  @Inject
  Bar bar;

  String ping() {
    return bar.ping();
  }
}

@ExtendWith(WeldJunit5Extension.class)
class TestClassProducerTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Foo.class, MockBeanTest.class).build();

    @ApplicationScoped
    @Produces
    Bar produceBar() {
      // Mock object provided by Mockito
      return Mockito.when(Mockito.mock(Bar.class).ping()).thenReturn("pong").getMock());
    }

    @Test
    public void testFoo() {
        Assert.assertEquals("pong", weld.select(Foo.class).get().ping());
    }
}
```

This should work in most of the cases (assuming the test class [meets some conditions](http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#what_classes_are_beans)) although it's a little bit cumbersome.
The second option is `WeldInitiator.Builder.addBeans(Bean<?>...)` which makes it possible to add beans during `AfterBeanDiscovery` phase easily.
You can provide your own `javax.enterprise.inject.spi.Bean` implementation or make use of existing solutions such as DeltaSpike [BeanBuilder](https://github.com/apache/deltaspike/blob/master/deltaspike/core/api/src/main/java/org/apache/deltaspike/core/util/bean/BeanBuilder.java) or for most use cases a convenient `org.jboss.weld.junit.MockBean` should be sufficient.
Use `org.jboss.weld.junit.MockBean.builder()` to obtain a new builder instance.

```java
interface Bar {
  String ping();
}

class Foo {
  @Inject
  Bar bar;

  String ping() {
    return bar.ping();
  }
}

@ExtendWith(WeldJunit5Extension.class)
class AddBeanTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Foo.class).addBeans(createBarBean()).build();

    static Bean<?> createBarBean() {
        return MockBean.builder()
                .types(Bar.class)
                .scope(ApplicationScoped.class)
                .creating(
                       // Mock object provided by Mockito
                       Mockito.when(Mockito.mock(Bar.class).ping()).thenReturn("pong").getMock())
                .build();
    }

    @Test
    public void testFoo() {
        assertEquals("pong", weld.select(Foo.class).get().ping());
    }
}
```

#### Adding mock interceptors

Sometimes it might be useful to add a mock interceptor, e.g. if an interceptor implementation requires some environment-specific features.
For this use case the `org.jboss.weld.junit.MockInterceptor` is a perfect match:

```
@FooBinding
class Foo {
   boolean ping() {
      return true;
   }
}

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@InterceptorBinding
@interface FooBinding {

   @SuppressWarnings("serial")
   static final class Literal extends AnnotationLiteral<FooBinding> implements FooBinding {
      public static final Literal INSTANCE = new Literal();
    };
   }
}

class MockInterceptorTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(Foo.class).addBeans(
            MockInterceptor.withBindings(FooBinding.Literal.INSTANCE).aroundInvoke((ctx, b) -> {
                return false;
                })).build();


    @Test
    public void testInterception() {
       Assert.assertFalse(weld.select(Foo.class).get().ping());
    }
}
```

## Additional Configuration

This section describes any additional configuration options this extension offers.

### Explicit Parameter Injection

As mentioned above, Weld is greedy when it comes to parameter injection.
It will claim the ability to resolve any parameter which is known as a bean type inside the running CDI container.
This is mainly for usability, as it would be annoying to constantly type additional annotations to mark which parameter should be injected and which should be left alone.

However, we are aware that this might cause trouble if more extensions are competing for parameter resolution.
In such case, you can turn on explicit parameter resolution and Weld will only resolve parameters which have at least one `javax.inject.Qualifier` annotation on them.
There are two ways to enable it; firstly, you can do it globally, through system property - `org.jboss.weld.junit5.explicitParamInjection=true`
This property is also available as a constant in our extension class, e.g. you can use `org.jboss.weld.junit5.WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION`.
Secondly, you can use `@ExplicitParamInjection` on your method, or test class.
In case of test class this annotation will enforce the presence on qualifiers on all methods.

Let's have a look at it:

```java
@EnableWeld
@ExplicitParamInjection // all methods will now require explicit parameters
class ExplicitParamInjectionTest {

    @Test
    public void testThatParamsAreNotResolvedByWeld(Foo foo) {
        // Weld will not attempt to resolve Foo, hence this test will fail unless there is another extension resolving it
    }

    @Test
    public void testThatParamsAreResolvedByWeld(@Default Foo foo, @MyQualifier Bar bar) {
        // Weld will resolve both of the parameters
    }
}
```

As you might know, if you want to inject a bean where you would normally not use any qualifier, you can do that using `@Default` qualifier (as shown in the code above).
This is in accordance with CDI specification, feel free to [read more about it](http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#builtin_qualifiers).
