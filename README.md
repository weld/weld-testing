# Weld JUnit Extensions

[![Travis CI Build Status](https://img.shields.io/travis/weld/weld-junit/master.svg)](https://travis-ci.org/weld/weld-junit)
[![Maven Central](http://img.shields.io/maven-central/v/org.jboss.weld/weld-junit4.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22weld-junit4%22)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The primary goal of this project is to provide simple and fast tools for CDI *unit/component* testing.
The tools are implemented as JUnit extensions.
Supports Weld **2.4** (CDI 1.2) and **3.0** (CDI 2.0).

## The What

Weld JUnit extension allows you to write unit tests for your beans regardless of the target environment (Java SE, Java EE, etc.).
It starts a real CDI container in minimal configuration meaning you can leverage all bean capabilities - injection, interception, events, etc. - without the need for mocking.
This extension boots up CDI container before each test method and shuts it down afterwards.
You have the power to customize what beans, extension, interceptors (and so on) are going to be in the container.
Furthermore, you can `@Inject` directly in your test class - and the list goes on...


## The Why

As a matter of fact, one could have a unit test for CDI bean without running a container, but that would present quite a few drawbacks.
Simulating field injection to start with, then interceptors and/or decorators - all in all, quite a challenge.
There are frameworks to make this easier such as [Mockito](http://site.mockito.org/); but use too many mocks and things get tangled real quick.
So we came with JUnit extension which allows you to use actual CDI container instead of complex simulations.
There is no need to change the way you develop your CDI components if you have a real container to test it with.
Besides, it's easy to combine this approach with mocking frameworks (see also [Adding mock beans](#adding-mock-beans)).

## Table of contents

* [JUnit 4](#junit-4)
  * [WeldInitiator](#weldinitiator)
    * [Flat Deployment](#flat-deployment)
    * [Convenient Starting Points](#convenient-starting-points)
      * [Test class injection](#test-class-injection)
      * [Activating context for a normal scope](#activating-context-for-a-normal-scope)
      * [Adding mock beans](#adding-mock-beans)

## JUnit 4

```xml
<dependency>
  <groupId>org.jboss.weld</groupId>
  <artifactId>weld-junit4</artifactId>
  <version>${version.weld-junit}</version>
</dependency>
```

### WeldInitiator

`org.jboss.weld.junit.WeldInitiator` is a `TestRule` (JUnit 4.9+) which allows to *start/stop* a Weld container per test method execution.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance.
By default, the container is optimized for testing purposes, i.e. with automatic discovery and concurrent deployment disabled (see also `WeldInitiator.createWeld()`).
However, it is possible to provide a customized `Weld` instance  - see also `WeldInitiator.of(Weld)` and `WeldInitiator.from(Weld)` methods.
`WeldInitiator` also implements `javax.enterprise.inject.Instance` and therefore might be used to perform programmatic lookup of bean instances.

#### Flat Deployment

Unlike when using [Arquillian Weld embedded container](https://github.com/arquillian/arquillian-container-weld), bean archive isolation is enabled by default.
This behaviour can be changed by setting a system property `org.jboss.weld.se.archive.isolation` to `false` or through the `Weld.property()` method.
In that case, Weld will use a _"flat"_ deployment structure - all bean classes share the same bean archive and all beans.xml descriptors are automatically merged into one.
Thus alternatives, interceptors and decorators selected/enabled for a bean archive will be enabled for the whole application.

#### Convenient Starting Points

A convenient static method `WeldInitiator.of(Class<?>...)` is also provided - in this case, the container is optimized for testing purposes and only the given bean classes are considered.

```java
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

class SimpleTest {

    @Rule
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

class AnotherSimpleTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.ofTestPackage();

    @Test
    public void testFoo() {
        // Alpha comes from the same package as AnotherSimpleTest
        assertEquals(1, weld.select(Alpha.class).ping());
    }

}
```

Furthermore, `WeldInitiator.Builder` can be used to customize the final `WeldInitiator` instance, e.g. to *activate a context for a given normal scope* or to *inject the test class*.

##### Test class injection

Sometimes, the programmatic lookup can imply unnecessary overhead, e.g. an annotation literal must be used for parameterized types and qualifiers with members.
`WeldInitiator.Builder.inject(Object)` instructs the rule to inject the given non-contextual instance once the container is started, i.e. during each test method execution:

```java
class InjectTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(Foo.class).inject(this).build();

  // Gets injected by WeldInitiator when testFoo() is about to be run
    @Inject
    @MyQualifier
    Foo foo;

    @Test
    public void testFoo() {
        assertEquals(42, foo.getValue());
    }
}
```

##### Activating context for a normal scope

`WeldInitiator.Builder.activate(Object)` makes it possible to activate and deactivate contexts for the specified normal scopes for each test method execution:

```java
class ContextsActivatedTest {

    @Rule
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

##### Adding mock beans

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

class TestClassProducerTest {

    @Rule
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
You can provide your own `javax.enterprise.inject.spi.Bean` implementation or make use of existing solutions such as DeltaSpike [BeanBuilder](https://github.com/apache/deltaspike/blob/master/deltaspike/core/api/src/main/java/org/apache/deltaspike/core/util/bean/BeanBuilder.java) or for most use cases a convenient `org.jboss.weld.junit4.MockBean` should be sufficient.
Use `org.jboss.weld.junit4.MockBean.builder()` to obtain a new builder instance.

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

class AddBeanTest {

    @Rule
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
