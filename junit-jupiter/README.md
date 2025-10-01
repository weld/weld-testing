# Weld JUnit 5 (Jupiter) Extensions

There are two extension here, both of which follow the extension mechanism introduced in JUnit 5.
Therefore, in order to use this extension in your test, you have to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)` or `@ExtendWith(WeldJunit5AutoExtension.class)` respectively.
In their default behaviour, the extensions will automatically start/stop Weld SE container and inject into all your `@Inject` fields and method parameters in the given test instance.
Furthermore, you can provide configuration and modify Weld bootstrapping process in various ways - extensions, scope activation, interception, ...

These JUnit extensions supports both test lifecycles [as described by JUnit 5](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle) - per method and per class.

These extensions fully support parallel execution mode of JUnit Jupiter, except for if `WeldContainer.current()` is
used which does not work if multiple containers are running from the same class loader. This includes the usage of
`MockBean.Builder#useUnmanaged` with the default create function and also `MockBean.read` which calls `useUnmanaged`
internally, as soon as the according beans are resolved and thus created. If either of these are used, you will
get a `WELD-ENV-002016` error if another container is running at the same time, so you should use `@Isolated`
for these features to make sure they are running individually.

Requirements are JUnit 5 and Java 17.

## Table of contents

* [Maven Artifact](#maven-artifact)
* [Configuration Versus Automagic](#configuration-versus-automagic)
* [WeldJunit5Extension](#weldjunit5extension)
  * [WeldInitiator](#weldinitiator)
    * [Convenient Starting Points](#convenient-starting-points)
      * [Test Class Injection](#test-class-injection)
      * [Activating Context for a Normal Scope](#activating-context-for-a-normal-scope)
      * [Adding mock beans](#adding-mock-beans)
      * [Adding mock interceptors](#adding-mock-interceptors)
      * [Mock injection services](#mock-injection-services)
    * [Inheritance](#inheritance-of-test-classes)
    * [Nested test classes](#nested-test-classes)
* [WeldJunit5AutoExtension](#weldjunit5autoextension)
  * [`@ActivateScopes`](#activatescopes)
  * [`@AddBeanClasses`](#addbeanclasses)
  * [`@AddEnabledDecorators`](#addenableddecorators)
  * [`@AddEnabledInterceptors`](#addenabledinterceptors)
  * [`@AddExtensions`](#addextensions)
  * [`@AddPackages`](#addpackages)
  * [`@EnableAlternativeStereotypes`](#enablealternativestereotypes)
  * [`@EnableAlternatives`](#enablealternatives)
  * [`@ExcludeBean`](#excludebean)
  * [`@ExcludeBeanClasses`](#excludebeanclasses)
* [Additional Configuration](#additional-configuration)
  * [Explicit Parameter Injection](#explicit-parameter-injection)
  * [Flat Deployment](#flat-deployment)

## Maven Artifact

```xml
<dependency>
  <groupId>org.jboss.weld</groupId>
  <artifactId>weld-junit5</artifactId>
  <version>${version.weld-junit}</version>
</dependency>
```

## Configuration Versus Automagic

There are two extensions you can choose from.
While both are ultimately achieving the same thing, each opts for a different approach.

`WeldJunit5Extension.class` is the original one where you declaratively configure the container, much like booting Weld SE itself.
There are some additional builder patterns on top of that allowing for easy addition of mocked beans and such.
The advantage of this approach is that you have complete control over what gets into the Weld container and can easily change that.
On the other hand, it may be rather verbose and requires you to add a specifically annotated field to every test (described below).

`WeldJunit5AutoExtension.class` is more of an annotation based approach where you don't need any special field in your test class.
In fact, you don't need anything except the JUnit `@ExtendWith` and our extension will try its best to find out what classes should be added to the Weld container as beans.
This of course makes some assumptions on your tests which may not always be met, hence there is a bunch of annotations which allow you to configure the container further.
Advantages of this approach are quick setup for basic cases, less verbose code and that eerie feeling that things are happening automagically.
On the not so bright side, automatic config is not almighty and in some cases will falter, forcing you to add some configuration via annotations.
Last but not least, overly complex test scenarios may mean loads of annotations, and you may be better off with the former extension.

No matter what extension you choose, do not mix them together!

## WeldJunit5Extension

The simplest way to use this extension is to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)`.
If you are in for shorter annotations, you can also use `@EnableWeld`.
With just these annotations, the Weld container will be started before each test is run and stopped afterwards.

This default behaviour includes:
* Bootstrap Weld SE container with
  * Disabled discovery
  * Disabled concurrent deployment
  * Added test class package as source of beans
* Inject into all fields of the test instance that are annotated with `@Inject`
* Inject into method parameters of your test methods
  * If the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in the container
    * An exception to this rule is `@ParameterizedTest` where Weld requires explicitly stating CDI qualifiers for each method parameter which should be injected
  * If this behaviour should be different, refer to [additional configuration section](#explicit-parameter-injection)
* Shut down the container after test is done

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

### WeldInitiator

`org.jboss.weld.junit5.WeldInitiator` is an entry point you will want to define if you wish to customize how we bootstrap Weld.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance.
By default, the container is optimized for testing purposes, i.e. with automatic discovery and concurrent deployment disabled (see also `WeldInitiator.createWeld()`).
However, it is possible to provide a customized `Weld` instance  - see also `WeldInitiator.of(Weld)` and `WeldInitiator.from(Weld)` methods.
`WeldInitiator` also implements `jakarta.enterprise.inject.Instance` and therefore might be used to perform programmatic lookup of bean instances.

`WeldInitiator` should be available in a field annotated with `@org.jboss.weld.junit5.WeldSetup`.
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

#### Convenient Starting Points

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

##### Test Class Injection

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

##### Activating Context for a Normal Scope

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

##### Adding mock beans

Sometimes you might need to add a mock for a bean that cannot be part of the test deployment, e.g. the original bean implementation has dependencies which cannot be satisfied in the test environment.
Very often, it's an ideal use case for mocking libraries, i.e. to create a bean instance with the desired behavior.
In this case, there are two options.
The first option is to add a [producer method](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#producer_method) or [field](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#producer_field) to the test class and add the test class to the deployment.
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
    public WeldInitiator weld = WeldInitiator.from(Foo.class, TestClassProducerTest.class).build();

    @ApplicationScoped
    @Produces
    Bar produceBar() {
      // Mock object provided by Mockito
      return Mockito.when(Mockito.mock(Bar.class).ping()).thenReturn("pong").getMock();
    }

    @Test
    public void testFoo() {
        Assertions.assertEquals("pong", weld.select(Foo.class).get().ping());
    }
}
```

This should work in most of the cases (assuming the test class [meets some conditions](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#what_classes_are_beans)) although it's a little bit cumbersome.
The second option is `WeldInitiator.Builder.addBeans(Bean<?>...)` which makes it possible to add beans during `AfterBeanDiscovery` phase easily.
You can provide your own `jakarta.enterprise.inject.spi.Bean` implementation or, for most use cases, a convenient `org.jboss.weld.junit.MockBean` should be sufficient.
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

##### Adding mock interceptors

Sometimes it might be useful to add a mock interceptor, e.g. if an interceptor implementation requires some environment-specific features.
For this use case the `org.jboss.weld.junit.MockInterceptor` is a perfect match:

```java

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
  final class Literal extends AnnotationLiteral<FooBinding> implements FooBinding {
    public static final Literal INSTANCE = new Literal();
  }
}
}

@EnableWeld
class MockInterceptorTest {

  @WeldSetup
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

##### Mock injection services

If a bean under the test declares a non-CDI injection point (such as `@Resource`) a mock injection service must be installed.
`WeldInitiator.Builder` comes with several convenient methods which allow to easily mock the Weld SPI:

* `bindResource()` - to handle `@Resource`
* `setEjbFactory()` - to handle `@EJB`
* `setPersistenceUnitFactory()` - to handle `@PersistenceUnit`
* `setPersistenceContextFactory()` - to handle `@PersistenceContext`

```java
class Baz {

    @Resource(lookup = "somejndiname")
    String coolResource;

}

@EnableWeld
class MyTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(Baz.class).bindResource("somejndiname", "coolString").build();

    @Test
    public void test(Baz baz) {
       Assertions.assertEquals("coolString", baz.coolResource);
    }
}
```

#### Inheritance of test classes

The `@WeldSetup` field can be defined in a superclass, but there can only be one `@WeldSetup` field in the class
hierarchy.

```java
abstract class GenericTest {
  @WeldSetup
  protected WeldInitiator weldInitiator = WeldInitiator.of(Foo.class);
}

@EnableWeld
class SpecializedTest extends GenericTest {

  @Inject
  Foo foo; // injected by the inherited Weld container

  // This would throw an exception, because there would be two @WeldSetup fields in the class hierarchy
  //
  // @WeldSetup
  // WeldInitiator secondWeldInitiator = WeldInitiator.of(Foo.class, Bar.class);
}
```

#### Nested test classes

A `@Nested` test class can have its own `WeldInitiator` field to configure a Weld container for the test methods of the
nested class. If there is no `WeldInitiator` field in the class hierarchy of the nested class, the enclosing class (and
its class hierarchy) is queried for *its* `WeldInitiator`
field and so on until the outermost test class is reached.

```java

@EnableWeld
class OuterTest {

  @WeldSetup
  WeldInitiator weld = WeldInitiator.of(Foo.class);

  @Test
  void test(Foo myFoo) {
    assertNotNull(myFoo);
    assertThrows(UnsatisfiedResolutionException.class, () -> outerWeld.select(Bar.class).get());
  }

  @Nested
  class InnerTestWithoutItsOwnInitiator {

    @Test
    void testInnerMethodWithOuterWeldContainer(Foo myFoo) {
      assertNotNull(myFoo);
    }
  }

  @Nested
  class InnerTestWithItsOwnInitiator {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(Foo.class, Bar.class);

    @Test
    void testInnerMethodWithInnerWeldContainer(Foo myFoo, Bar myBar) {
      assertNotNull(myFoo);
      assertNotNull(myBar);
    }
  }
}
```

**Attention**: There is only one Weld container for every test so no matter if the inner or the outer `WeldInitiator` field is chosen to initialize that container, it is used to fill the injection points in the nested class *and* its enclosing class(es). Therefore, you have to make sure that all beans that the outer class(es) requires are also present in the inner `WeldInitiator`!

```java
class OuterTest {

  @WeldSetup
  WeldInitiator outerWeld = WeldInitiator.of(Foo.class);

  @Inject
  Foo foo;

  @Nested
  class InnerTest {

    @WeldSetup
    WeldInitiator innerWeld = WeldInitiator.of(Bar.class);

    // This test fails with an exception, because OuterTest.this.foo
    // cannot be injected by the inner Weld container.
    //
    // @Test
    // void testAnything() {
    //   Assertions.assertTrue(true);
    // }
  }
}
```

## WeldJunit5AutoExtension

To use this approach, annotate your test class with `@ExtendWith(WeldJunit5AutoExtension.class)` or just `@EnableAutoWeld`.
By default, the extension will:

* Inspect your test class and try to figure out what bean classes it needs based on injection points (field and parameter injection both work)
  * This is done by finding classes and verifying whether they have a [bean defining annotation](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#bean_defining_annotations) so make sure they do
* Add those classes to the Weld container
* Process additional annotations on the test class and also on each discovered class
  * `@AddPackages`, `@AddExtensions`, `@ActivateScopes`, ...
* Annotates test classes with `@Singleton` and prevents another instantiation by Weld and instead substitutes the test instances provided by JUnit
* Bootstrap Weld SE container with
  * Disabled discovery
  * Disabled concurrent deployment
  * Added test class package as source of beans
* Inject into all fields of the test instance that are annotated with `@Inject`
* Inject into method parameters of your test methods
  * If the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in the container
  * If this behaviour should be different, refer to [additional configuration section](#explicit-parameter-injection)
* Shut down the container after test is done

Here is a simple example using the default plus one additional annotation (`@AddPackages`):

```java
import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
@AddPackages(Engine.class) // turn all legitimate classes inside Engine's package into CDI beans
public class BasicAutomagicTest {

  @Inject
  private V8 v8Engine;

  @Inject
  private V6 v6Engine;

  @Test
  void test() {
    assertNotNull(v8Engine);
    assertNotNull(v6Engine);
  }

}
```

The default behaviour is powerful enough to handle basic cases where you simply want to inject a bean and make assertions on it.
However, it will not be enough if you want to, say, test your CDI extensions, enable custom interceptors or make sure certain scopes are active.
Or if you want to inject interfaces instead of implementations of beans.
For those cases, and many others, there are special annotations you can use - we will go over them, one at a time.
At the end there is an example showing several of them.

### `@ActivateScopes`

Normally, only `@ApplicationScoped` and `@Dependent` beans work without any additional settings.
`@ActivateScopes` annotation allows you to list scopes which are to be activated for the duration of the test.
Note that the duration is dependent on your setting of JUnit test lifecycle - it can be either per method or per class.

### `@AddBeanClasses`

Using this annotation you can specify a list of Java classes which will be registered as beans with Weld container.
Note that standard rules for beans apply (proxiability for instance).

This can be handy if you wish to operate with interfaces rather than implementation classes as the class scanning performed by the extension
cannot know for sure which class is the implementation of given interface.

### `@AddEnabledDecorators`

Adds the decorator class into deployment and enables it.

### `@AddEnabledInterceptors`

Adds the interceptor class into deployment and enables it.

### `@AddExtensions`

Registers one or more extensions within the Weld container. This is the programmatic replacement for placing the extension in `META-INF`.

### `@AddPackages`

Adds all bean classes from listed packages to the Weld container.
Packages are selected by providing any bean class in the package.
You can also specify if this should be done recursively using the `recursive` parameter.

### `@EnableAlternativeStereotypes`

Enables given alternative stereotypes.

### `@EnableAlternatives`

Selects given alternatives for the test bean archive.

### `@ExcludeBean`

Excludes a bean, or multiple beans, that include a bean defining annotation (e.g. scope) from automatic discovery.
This can be helpful to allow replacing a bean class with a different implementation, typically a mock.

The type of bean to exclude is implied by the annotated field's type or annotated method's return type.
If the type is a base class or interface all beans extending/implementing that type will be excluded.

NOTE: This annotation will only exclude beans defined by class annotations.
It will not exclude beans of the implied type that are defined by `@Produces` producer methods/fields or synthetic beans.
Also, the current implementation excludes beans based on type, disregarding any qualifiers that are specified.

```java
import org.jboss.weld.junit5.auto.ExcludeBean;
import org.jboss.weld.junit5.auto.WeldJunit5AutoExtension;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@EnableAutoWeld
class TestSomeFoo {

  @Inject
  SomeFoo someFoo;   // SomeFoo depends upon application scoped bean Foo

  @Produces
  @ExcludeBean   // Excludes beans with type Foo from automatic discovery
  Foo mockFoo = mock(Foo.class);  // mockFoo is now produced in place of original Foo impl

  @Test
  void test(Foo myFoo) {
    assertNotNull(myFoo);
    assertEquals(myFoo.getBar(), "mock-foo");
  }
}
```

### `@ExcludeBeanClasses`

Excludes a set of classes with bean defining annotations (e.g. scopes) from automatic discovery.
This can be helpful to allow replacing bean classes with a different implementation; typically a mock.

This annotation works as an inverse of [`@AddBeanClasses`](#addbeanclasses) hence usually requires actual bean implementation classes as parameters.

NOTE: This annotation will only exclude beans defined by class annotations.
It will not exclude beans of the specified type that are defined by `Produces` producer methods/fields or synthetic beans.

## Additional Configuration

This section describes any additional configuration options this extension offers.

### Explicit Parameter Injection

As mentioned above, Weld is greedy when it comes to parameter injection.
It will claim the ability to resolve any parameter which is known as a bean type inside the running CDI container except the ones built into JUnit itself.
This is mainly for usability, as it would be annoying to constantly type additional annotations to mark which parameter should be injected and which should be left alone.

However, we are aware that this might cause trouble if more extensions are competing for parameter resolution.
In such case, you can turn on explicit parameter resolution and Weld will only resolve parameters which have at least one `jakarta.inject.Qualifier` annotation on them.
There are two ways to enable it:
* First option is enabling this globally through a system property - `org.jboss.weld.junit5.explicitParamInjection=true`
This property is also available as a constant in our extension class; you can therefore refer to it via `org.jboss.weld.junit5.WeldJunit5Extension.GLOBAL_EXPLICIT_PARAM_INJECTION`.
* The other approach is to use `@ExplicitParamInjection(boolean)` on either test method, or test class.
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
This is in accordance with the CDI specification, feel free to [read more about it](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#builtin_qualifiers).

Last but not least, nested classes will automatically inherit this behavior from their enclosing class. They are however free to override this by declaring the annotation and its respective value themselves.

### Flat Deployment

Unlike [Arquillian Weld embedded container](https://github.com/arquillian/arquillian-container-weld), weld-junit has bean archive isolation enabled by default.
This behaviour can be changed by setting the system property `org.jboss.weld.se.archive.isolation` to `false` or through the `Weld.property()` method.
If set to `false`, Weld will use a _"flat"_ deployment structure - all bean classes share the same bean archive and all `beans.xml` descriptors are automatically merged into one.
Thus, alternatives, interceptors, and decorators selected / enabled for a bean archive will be enabled for the whole application.
Note that this configuration only makes a difference if you run with *enabled discovery*; it won't affect your deployment if you use the synthetic bean archive.

## Limitations

* `@Produces`, `@Disposes`, and `@Observes` don't work in `@Nested` test classes which fail to meet [valid bean](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#what_classes_are_beans) requirements due to the lack of a no-arg constructor and Weld ignores them silently. However, `@Inject` and parameter injection also work with `@Nested` classes.
