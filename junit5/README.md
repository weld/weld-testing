# Weld JUnit 5 Extensions

There are two extension here, both of which follow the extension mechanism introduced in JUnit 5.
Therefore, in order to use this extension in your test, you have to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)` or `@ExtendWith(WeldJunit5AutoExtension.class)` respectively.
In their default behaviour, the extensions will automatically start/stop Weld SE container and inject into all your `@Inject` fields and method parameters in the given test instance.
Furthermore you can provide configuration and modify Weld bootstrapping process in various ways - extensions, scope activation, interception, ...

The extensions support both test lifecycles - per method and per class.

Requirements are JUnit 5 and Java 8.

## Table of contents

* [Maven Artifact](#maven-artifact)
* [Configuration Versus Automagic](#configuration-versus-automagic)
* [WeldJunit5Extension](#weldjunit5extension)
  * [WeldInitiator](#weldinitiator)
    * [Convenient Starting Points](#convenient-starting-points)
      * [Test class injection](#test-class-injection)
      * [Activating context for a normal scope](#activating-context-for-a-normal-scope)
      * [Adding mock beans](#adding-mock-beans)
      * [Adding mock interceptors](#adding-mock-interceptors)
      * [Mock injection services](#mock-injection-services)
* [WeldJunit5AutoExtension](#weldjunit5autoextension)
  * [`@ActivateScopes`](#activate-scopes)
  * [`@AddBeanClasses`](#add-bean-classes)
  * [`@AddEnabledDecorators`](#add-enabled-decorators)
  * [`@AddEnabledInterceptors`](#add-enabled-interceptors)
  * [`@AddExtensions`](#add-extensions)
  * [`@AddPackages`](#add-packages)
  * [`@EnableAlternativeStereotypes`](#enable-alternative-stereotypes)
  * [`@EnableAlternatives`](#enable-alternatives)
  * [`@OverrideBean`](#override-bean)
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
The advantage of this approach is that you have complete control over what gets into Weld container and can easily change that.
On the other hand, it may be rather verbose and requires you to add a specifically annotated field to every test (described below).

`WeldJunit5AutoExtension.class` is more of an annotation based approach where you don't need any special field in your test class.
In fact, you don't need anything except the JUnit `@ExtendWith` and our extension will try its best to find out what classes should be added to Weld container as beans.
This of course makes some assumptions on your tests which may not always be met, hence there is bunch of annotations which allow you to configure the container.
Pros of this approach are quick setup for basic cases, less verbose code and that eerie feeling that things are happening automagically.
On the not so bright side, automatic config is not almighty and in some cases will falter forcing you to add some configuration via annotations.
Last but not least, overly complex test scenarios may mean loads of annotations and you may be better off with the former extension.

No matter what extension you choose, do not mix them together!

## WeldJunit5Extension

The simplest way to use this extension is to annotate your test class with `@ExtendWith(WeldJunit5Extension.class)`.
If you are in for shorter annotations, you can also use `@EnableWeld`.
With just these annotations, Weld container will be started before each test is run and stopped afterwards.
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

### WeldInitiator

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

##### Test class injection

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

##### Activating context for a normal scope

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

##### Adding mock interceptors

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
`WeldInitiator` builder comes with several convenient methods which allow to easily mock the Weld SPI:

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
## WeldJunit5AutoExtension

To use this approach, annotate your test class with `ExtendWith(WeldJunit5AutoExtension.class)` or just `@EnableAutoWeld`.
By default, the extension will:

* Inspect your test class and try to figure out what beans classes it needs based on injection points (field and parameter injection both work)
  * This is done by finding classes and verifying if they have [bean defining annotation](http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#bean_defining_annotations) so make sure they do
* Add those classes to Weld container
* Process additional annotations on test class
  * `@AddPackages`, `@AddExtensions`, `@ActivateScopes`, ...
* Bootstrap Weld container
* Inject into test instance, e.g. into all `@Inject` fields
* Inject into method parameters of your test methods
  * In case the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in CDI container
  * If you wish to change this behaviour, please refer to [additional configuration section](#explicit-parameter-injection)
* Shutting down the container after test is done

Here is a simple example using the default plus one additional annotation (`@AddPackages`):

```java
import org.jboss.weld.junit5.auto.beans.Engine;
import org.jboss.weld.junit5.auto.beans.V8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

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
    assertNotNull(V8Engine);
    assertNotNull(v6Engine);
  }

}
```

The default behaviour is powerful enough to handle basic cases where you simply want to inject a bean make assertions on it.
However, it will not be enough if you want to, say, test your CDI extensions, enable custom interceptors or make sure certain scopes are active.
Or if you want to inject interfaces instead of implementations of beans.
For those cases, and many others, there are special annotations you can use - we will go over them, one at a time.
At the end there is an example showing several of them.

### `@ActivateScopes`

Normally, only `@ApplicationScoped` and `@Dependent` beans work without any additional settings.
`@ActivateScopes` annotation allows you to list scopes which are to be actived for the duration of the test.
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

Registers one or more extensions within Weld container; this is programmatic replacement for placing the extension in `META-INF`.

### `@AddPackages`

Adds all bean classes from listed packages to Weld container.
Packages are selected by providing any bean class in the package.
You can also specify if this should be done recursively using the `recursive` parameter.

### `@EnableAlternativeStereotypes`

Enables given alternative stereotype.

### `@EnableAlternatives`

Selects and alternative for the test bean archive.

### `@OverrideBean`

This annotations covers a rather specific use case, it allows to "override a bean" which may otherwise be included in the container.
It usually goes hand in hand with `@Produces` which is a natural way to provide a replacement bean.
Strictly speaking, this is just an always enabled alternative stereotype.
Let's look at a code snippet:

```java
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.OverrideBean;
import org.jboss.weld.junit5.auto.WeldJunit5AutoExtension;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

import static org.junit.jupiter.api.Assertions.assertNotNull;

 @EnableAutoWeld
 @AddPackages(Foo.class) // this brings in the *original* Foo impl you want to overide
 class OverrideFooTest {
 
   @Produces
   @OverrideBean
   Foo fakeFoo = new Foo("non-baz"); // this will be an enabled alternative we provide instead
 
   @Test
   void test(Foo myFoo) {
     assertNotNull(myFoo);
     assertEquals(myFoo.getBar(), "non-baz");
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

#### Flat Deployment

Unlike [Arquillian Weld embedded container](https://github.com/arquillian/arquillian-container-weld), weld-junit has bean archive isolation enabled by default.
This behaviour can be changed by setting a system property `org.jboss.weld.se.archive.isolation` to `false` or through the `Weld.property()` method.
If set, Weld will use a _"flat"_ deployment structure - all bean classes share the same bean archive and all beans.xml descriptors are automatically merged into one.
Thus alternatives, interceptors and decorators selected/enabled for a bean archive will be enabled for the whole application.
Note that this configuration only makes difference if you run with *enabled discovery*; it won't affect your deployment if you use synthetic bean archive.
