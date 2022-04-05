# Weld Spock Extension

There is one global Spock extension in this module, the behavior of which can be configured using various annotations.
By default, the extension has no effect at all. To use the extension you either have to annotate some specification
or feature with `@EnableWeld`, or enable the processing for all specifications (unless disabled specifically) using
the Spock configuration file.

In their default behaviour, the `@EnableWeld` annotation will cause a Weld SE container to be started and stopped
for each iteration of each feature where the extension is configured to run. The package of the specification will be
searched for beans and those added to the container. All `@Shared` and non-`@Shared` fields of the specification and
super specifications that are annotated with `@Inject` will be injected. All method parameters of all fixture and
feature methods that are no data variables and not yet provided by a different Spock extension and are resolvable
in the container are injected automatically too.

Furthermore, you can provide explicit configuration for the Weld container, or modify the configuration in various ways
as further described below - extensions, scope activation, interception, ...

The extension fully supports parallel execution mode of Spock, except for if `WeldContainer.current()` is used which
does not work if multiple containers are running from the same class loader. This includes the usage of
`MockBean.Builder#useUnmanaged` with the default create function and also `MockBean.read` which calls `useUnmanaged`
internally, as soon as the according beans are resolved and thus created. If either of these are used, you will
get a `WELD-ENV-002016` error if another container is running at the same time, so you should use `@Isolated`
for these features to make sure they are running individually.

Minimum requirements are Spock 2 and Java 8.

## Table of contents

* [Maven Artifact](#maven-artifact)
* [Enabling and Disabling the Extension](#enabling-and-disabling-the-extension)
* [Scope of the Started Weld Container](#scope-of-the-started-weld-container)
  * [Iteration Scope](#iteration-scope)
  * [Feature Scope](#feature-scope)
  * [Specification Scope](#specification-scope)
* [Configuration Versus Automagic](#configuration-versus-automagic)
* [The Manual Mode](#the-manual-mode)
  * [WeldInitiator and @WeldSetup](#weldinitiator-and-weldsetup)
    * [Convenient Starting Points](#convenient-starting-points)
      * [Test Class Injection](#test-class-injection)
      * [Activating Context for a Normal Scope](#activating-context-for-a-normal-scope)
      * [Adding mock beans](#adding-mock-beans)
      * [Adding mock interceptors](#adding-mock-interceptors)
      * [Mock injection services](#mock-injection-services)
* [The Automagic Mode](#the-automagic-mode)
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
* [IllegalStateException in Assertion Failure Rendering](#illegalstateexception-in-assertion-failure-rendering)

## Maven Artifact

```xml
<dependency>
    <groupId>org.jboss.weld</groupId>
    <artifactId>weld-spock</artifactId>
    <version>${version.weld-spock}</version>
</dependency>
```

## Enabling and Disabling the Extension

If a feature has an `@EnableWeld` or `@DisableWeld` annotation applied, this is what is effective.
If Weld is disabled, no container will be started for the feature or the iterations and no non-`@Shared` fields,
or method parameters will be injected. However, if for the specification a `SPECIFICATION` scoped Weld
was booted due to annotation or Spock configuration file options,
this container will have injected instances into the `@Shared` fields already.

If a feature has neither of the two annotations applied, the configuration of the specification is inherited.

If a specification has an `@EnableWeld` or `@DisableWeld` annotation applied, this is effective
for all features that do not have their own annotation. It will have the same effect as if the annotation
is copied to all features that have none of the two annotations already, except if scope `SPECIFICATION` is selected,
as this is only valid in a specification level annotation or the Spock configuration file.

If a specification has neither of the two annotations applied, the super specifications are searched in order
and if an annotated one is found, its annotation is effective as if it were on the specification directly.

In case no annotation is found in super specifications, the settings from the Spock configuration file
or the respective default settings are effective. To enable the extension with the detail options from the configuration
file or the default on all specifications that don't have either annotation and there on all features that don't have
either, the configuration file entry looks like:
```groovy
'org.jboss.weld' {
    enabled true
}
```

If an element is annotated with both of these annotations, an exception is thrown,
as it is unclear which should have precedence.

If a class that is no specification or a method that is no feature is annotated with either of those annotations,
they are simply ignored and have no effect at all.

The properties of the `@EnableWeld` annotation, the Spock configuration file options, and the additional annotations
as described below can be used to further specify the behavior of the extension.

## Scope of the Started Weld Container

Using the `scope` configuration file option if the global configuration is effective, or the `scope` property of the
`@EnableWeld` annotation if it is effective, the scope of the started Weld container can be configured.

### Iteration Scope

By default, the scope is `ITERATION`, which means that before an iteration (before the `setup` method is called)
a new Weld container will be started, exclusively used for this iteration and after the iteration
(after the `cleanup` method is called) shut down again.

The `@Shared` fields will be injected, but only at the time the iteration starts running. If you have multiple
such Weld instances in one specification and use the parallel execution feature of Spock, those Weld containers might
overwrite each other's values, so be careful what you configure to not get unexpected results.

The non-`@Shared` fields will also be injected, as well as method parameters of `setup`, feature, and `cleanup` methods.

`setupSpec` and `cleanupSpec` method parameters will not be injected as at the time those methods are executed,
the Weld container is either not yet running or already shut down.

### Feature Scope

Before a feature (before the `setup` method of the first iteration is called) a new Weld container will be started,
exclusively used for all iterations of this feature and after the feature (after the `cleanup` method
of the last iteration is called) shut down again.

The `@Shared` fields will be injected, but only at the time the first iteration starts running. If you have multiple
such Weld instances in one specification and use the parallel execution feature of Spock, those Weld containers might
overwrite each other's values, so be careful what you configure to not get unexpected results.

The non-`@Shared` fields will also be injected, as well as method parameters of `setup`, feature, and `cleanup` methods.

`setupSpec` and `cleanupSpec` method parameters will not be injected as at the time those methods are executed,
the Weld container is either not yet running or already shut down.

### Specification Scope

Before a specification (before the `setupSpec` method is called) a new Weld container will be started,
exclusively used for all iterations of all features of this specification and after the specification
(after the `cleanupSpec` method is called) shut down again.

The `@Shared` fields will be injected once after the container was booted successfully.

The non-`@Shared` fields will also be injected, as well as method parameters of feature, and all fixture methods.

This scope can only be selected on a specification or in the Spock configuration file.
If it is used for a feature annotation, an exception will be thrown.

_**Example:**_
```groovy
import static org.jboss.weld.spock.EnableWeld.Scope.SPECIFICATION

'org.jboss.weld' {
    scope SPECIFICATION
}
```

## Configuration Versus Automagic

The configuration file section and the `@EnableWeld` annotation have a boolean option `automagic` with which one of two
modes can be selected that ultimately achieve the same thing, but each opt for a different approach.

By default, the value is `false` and a declarative configuration mode is chosen, much like booting Weld SE itself. There
are some additional builder patterns on top of that allowing for easy addition of mocked beans and such. The advantage
of this approach is that you have complete control over what gets into the Weld container and can easily change that. On
the other hand, it may be rather verbose and requires you to add a specifically annotated field to every specification.

If the option is set to `true`, a more annotation based approach is used, where no special field in the specification is
necessary. In fact, often nothing except for the `@EnableWeld(automagic = true)` annotation is necessary and this
extension will do its best to find out what classes should be added to the Weld container as beans.
This of course makes some assumptions on your specifications which may not always be met, hence there is a bunch of
annotations which allow you to configure the container further.
Advantages of this approach are quick setup for basic cases, less verbose code and that eerie feeling that things
are happening automagically. On the not so bright side, automatic config is not almighty and in some cases will falter,
forcing some configuration to be done via annotations.
Last but not least, overly complex test scenarios may mean loads of annotations, and you may be better off with the
declarative approach.

## The Manual Mode

The simplest way to use this extension is to annotate your specification with `@EnableWeld`.
With just this annotation, the Weld container will be started before each iteration is run and stopped afterwards.

This default behaviour includes:
* Bootstrap Weld SE container with
  * Disabled discovery
  * Disabled concurrent deployment
  * Added specification package as source of beans
* Inject into all `@Shared` fields of the specification that are annotated with `@Inject`
* Inject into all non-`@Shared` fields of the specification that are annotated with `@Inject`
* Inject into `setup`, feature, and `cleanup` method parameters of your specification
  * If the parameter is no data variable
  * If no other Spock extension already provided a value for it
  * If the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in the container
  * If this behaviour should be different, refer to [additional configuration section](#explicit-parameter-injection)
* Shut down the container after iteration is done

_**Example:**_
```groovy
@EnableWeld
class BasicUsageTest extends Specification {
    @Inject
    MyBean myBean

    def 'test foo'(MyOtherBean otherBean) {
        // Weld SE container is bootstrapped here and the injection points are resolved
        expect:
            true
    }
}
```

### WeldInitiator and @WeldSetup

`org.jboss.weld.spock.WeldInitiator` is the entry point with which the boostrap of Weld can be customized.
The container is configured through a provided `org.jboss.weld.environment.se.Weld` instance.
By default, the container is optimized for testing purposes, i.e. with automatic discovery and concurrent deployment
disabled (see also `WeldInitiator.createWeld()`). However, it is possible to provide a customized `Weld` instance
- see also `WeldInitiator.of(Weld)` and `WeldInitiator.from(Weld)` methods.
`WeldInitiator` also implements `Instance` and therefore might be used to perform programmatic lookup of bean instances.

`WeldInitiator` should be available in a field annotated with `@WeldSetup`. If the scope `SPECIFICATION` is effective,
or for a data-driven feature if scope `FEATURE` is effective, `@Shared` fields of the specification and its super
specifications are searched for exactly one field that is annotated with `@WeldSetup`.

If multiple such fields are found, an exception is thrown.

If exactly one is found, and its value is not of type `WeldInitiator`, an exception is thrown.

Otherwise, as the type is correct, it is used as-is to initialize the Weld container.

From there you can use static methods.

_**Example:**_
```groovy
@EnableWeld
class MyNewTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.of(SomeClass)

    def 'test foo'() {...}
}
```

#### Convenient Starting Points

A convenient static method `WeldInitiator.of(Class<?>...)` is also provided - in this case, the container is optimized
for testing purposes and only the given bean classes are considered.

_**Example:**_
```groovy
@EnableWeld
class SimpleTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.of(Foo)

    def 'test foo'() {
        // Note that Weld container is started automatically

        expect: 'WeldInitiator can be used to perform programmatic lookup of beans'
            weld.select(Foo).get().baz == 'baz'

        and: 'WeldInitiator can be used to fire a CDI event'
            weld.event().select(Baz).fire(new Baz())
    }
}
```

It's also possible to use `WeldInitiator.ofTestPackage()` - the container is optimized for testing purposes
and all the classes from the specification package are added automatically.

_**Example:**_
```groovy
@EnableWeld
class AnotherSimpleTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.ofTestPackage()

    def 'test foo'() {
        expect:
            // Alpha comes from the same package as AnotherSimpleTest
            weld.select(Alpha).ping() == 1
    }
}
```

Furthermore, `WeldInitiator.Builder` can be used to customize the final `WeldInitiator` instance,
e.g. to *activate a context for a given normal scope*.

##### Test Class Injection

Everytime the extension is effective for a feature, it will automatically resolve all `@Inject` fields of the
specification as well as attempt to resolve fixture and feature method parameters, should they be injectable beans,
no data variables and not yet provided by another extension.

_**Example:**_
```groovy
@EnableWeld
class InjectTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.from(Foo).build()

    // Gets injected before executing test
    @Inject
    @MyQualifier
    Foo foo

    def 'test foo'(Foo fooAsParam) {
        expect:
            foo.value == 42
            fooAsParam.value == 42
    }
}
```

##### Activating Context for a Normal Scope

`WeldInitiator.Builder.activate(Class<? extends Annotation>...)` makes it possible to activate and deactivate contexts
for the specified normal scopes for each iteration execution.

_**Example:**_
```groovy
@EnableWeld
class ContextsActivatedTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(Foo, Oof)
            .activate(RequestScoped, SessionScoped)
            .build()

    def 'contexts for @RequestScoped and @SessionScoped should be active'() {
        expect:
            // Foo is @RequestScoped
            weld.select(Foo).get().doSomethingImportant()
            // Oof is @SessionScoped
            weld.select(Oof).get().doSomethingVeryImportant()
    }
}
```

##### Adding mock beans

Sometimes it might be necessary to add a mock for a bean that cannot be part of the test deployment,
e.g. the original bean implementation has dependencies which cannot be satisfied in the test environment.
Very often, it's an ideal use case for the Spock build-in mocking and stubbing functionality,
i.e. to create a bean instance with the desired behavior and verify behavior.

In this case, there are two options.
* The first option is to add a
  [producer method](https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#producer_method)
  or [field](https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#producer_field) to the specification
  and add the specification as bean to the deployment. The test class will be recognized as a bean
  and therefore the producer will also be discovered.

  _**Example:**_
  ```groovy
  interface Bar {
      String ping()
  }

  class Foo {
      @Inject
      Bar bar

      String ping() {
          bar.ping()
      }
  }

  @EnableWeld
  class TestClassProducerTest extends Specification {
      @WeldSetup
      def weld = WeldInitiator.from(Foo, TestClassProducerTest).build()

      @ApplicationScoped
      @Produces
      Bar produceBar() {
          // Stub object provided by Spock
          Stub(Bar) {
              ping() >> 'pong'
          }
      }

      def 'test foo'() {
          expect:
              weld.select(Foo).get().ping() == 'pong'
      }
  }
  ```
  This should work in most of the cases (assuming the test class
  [meets some conditions](https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#what_classes_are_beans))
  although it's a little bit cumbersome.

* The second option is `WeldInitiator.Builder.addBeans(Bean<?>...)` which makes it possible to add beans during
  `AfterBeanDiscovery` phase easily. You can provide your own `Bean` implementation or, for most use cases,
  a convenient `org.jboss.weld.junit.MockBean` should be sufficient.
  `MockBean.builder()` can be used to obtain a new builder instance.

_**Example:**_
```groovy
interface Bar {
    String ping()
}

class Foo {
    @Inject
    Bar bar

    String ping() {
        bar.ping()
    }
}

@EnableWeld
class AddBeanTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator.from(Foo).addBeans(createBarBean()).build()

    static createBarBean() {
        MockBean
                .builder()
                .types(Bar)
                .scope(ApplicationScoped)
                .creating(Stub(Bar) {
                    ping() >> 'pong'
                })
                .build()
    }

    def 'test foo'() {
        expect:
            weld.select(Foo).get().ping() == 'pong'
    }
}
```

##### Adding mock interceptors

Sometimes it might be useful to add a mock interceptor, e.g. if an interceptor implementation requires some
environment-specific features. For this use case the `MockInterceptor` is a perfect match.

_**Example:**_
```groovy
@FooBinding
class Foo {
   boolean ping() {
      true
   }
}

@Target([TYPE, METHOD])
@Retention(RUNTIME)
@InterceptorBinding
@interface FooBinding {
   static final class Literal extends AnnotationLiteral<FooBinding> implements FooBinding {
      public static final Literal INSTANCE = new Literal()
   }
}

@EnableWeld
class MockInterceptorTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(Foo)
            .addBeans(MockInterceptor
                    .withBindings(FooBinding.Literal.INSTANCE)
                    .aroundInvoke { ctx, b -> false })
            .build()

    def 'interception should work properly'() {
        expect:
            !weld.select(Foo).get().ping()
    }
}
```

##### Mock injection services

If a bean under the test declares a non-CDI injection point (such as `@Resource`) a mock injection service must be
installed. `WeldInitiator.Builder` comes with several convenient methods which allow to easily mock the Weld SPI:

* `bindResource()` - to handle `@Resource`
* `setEjbFactory()` - to handle `@EJB`
* `setPersistenceUnitFactory()` - to handle `@PersistenceUnit`
* `setPersistenceContextFactory()` - to handle `@PersistenceContext`

_**Example:**_
```groovy
class Baz {
    @Resource(lookup = 'somejndiname')
    String coolResource
}

@EnableWeld
class MyTest extends Specification {
    @WeldSetup
    def weld = WeldInitiator
            .from(Baz)
            .bindResource('somejndiname', 'coolString')
            .build()

    def test(Baz baz) {
        expect:
            baz.coolResource == 'coolString'
    }
}
```

## The Automagic Mode

To use this approach, the `automagic` property is set to `true`, either in the Spock configuration file if the global
settings are effective or in the `@EnableWeld` annotation if it is effective.

By default, the extension will:
* Inspect the specification and try to figure out what bean classes it needs based on injection points
  (field and parameter injection both work)
  * This is done by finding classes and verifying whether they have a
    [bean defining annotation](https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#bean_defining_annotations),
    so make sure they do
* Add those classes to the Weld container
* Process additional annotations on the specification and also on each discovered class
  * `@AddPackages`, `@AddExtensions`, `@ActivateScopes`, ...
* Annotates specifications with `@Singleton` and prevents another instantiation by Weld,
  and instead substitutes the specification instances provided by Spock
* Bootstrap Weld SE container with
  * Disabled discovery
  * Disabled concurrent deployment
* Inject into all `@Shared` fields of the specification that are annotated with `@Inject`
* Inject into all non-`@Shared` fields of the specification that are annotated with `@Inject`
* Inject into `setup`, feature, and `cleanup` method parameters of your specification
  * If the parameter is no data variable
  * If no other Spock extension already provided a value for it
  * If the type of the parameter matches a known and resolvable bean
  * By default, Weld is greedy and will try to resolve all parameters which are known as bean types in the container
  * If this behaviour should be different, refer to [additional configuration section](#explicit-parameter-injection)
* Shut down the container after iteration is done

Here is a simple example using the default plus one additional annotation (`@AddPackages`).

_**Example:**_
```groovy
@EnableAutoWeld
@AddPackages(Engine) // turn all legitimate classes inside Engine's package into CDI beans
class BasicAutomagicTest extends Specification {
    @Inject
    private V8 v8Engine

    @Inject
    private V6 v6Engine

    def test() {
        expect:
            v8Engine != null
            v6Engine != null
    }
}
```

The default behaviour is powerful enough to handle basic cases where you simply want to inject a bean and make
assertions on it. However, it will not be enough if you want to, say, test your CDI extensions, enable custom
interceptors or make sure certain scopes are active. Or if you want to inject interfaces instead of implementations
of beans. For those cases, and many others, there are special annotations you can use - we will go over them,
one at a time. At the end there is an example showing several of them.

### `@ActivateScopes`

Normally, only `@ApplicationScoped` and `@Dependent` beans work without any additional settings.
`@ActivateScopes` annotation allows you to list scopes which are to be activated for the lifetime
of the Weld container which is depending on the `scope` setting you use.

### `@AddBeanClasses`

Using this annotation you can specify a list of classes which will be registered as beans with Weld container.
Note that standard rules for beans apply (proxiability for instance).

This can be handy if you wish to operate with interfaces rather than implementation classes as the class scanning
performed by the extension cannot know for sure which class is the implementation of given interface.

### `@AddEnabledDecorators`

Adds the decorator class into deployment and enables it.

### `@AddEnabledInterceptors`

Adds the interceptor class into deployment and enables it.

### `@AddExtensions`

Registers one or more extensions within the Weld container.
This is the programmatic replacement for placing the extension in `META-INF`.

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
This can be helpful to allow replacing a bean class with a different implementation, typically a mock or stub.

The type of bean to exclude is implied by the annotated field's type or annotated method's return type.
If the type is a base class or interface all beans extending / implementing that type will be excluded.

NOTE: This annotation will only exclude beans defined by class annotations.
It will not exclude beans of the implied type that are defined by `@Produces` producer methods / fields or synthetic
beans. Also, current the implementation excludes beans based on type, disregarding any qualifiers that are specified.

_**Example:**_
```groovy
@EnableWeld(automagic = true)
class TestSomeFoo extends Specification {
    // SomeFoo depends upon application scoped bean Foo
    @Inject
    SomeFoo someFoo

    @Produces
    // Excludes beans with type Foo from automatic discovery
    @ExcludeBean
    // mockFoo is now produced in place of original Foo impl
    Foo mockFoo = Stub()

    def test(Foo myFoo) {
        expect:
            myFoo?.bar == 'mock-foo'
    }
}
```

### `@ExcludeBeanClasses`

Excludes a set of classes with bean defining annotations (e.g. scopes) from automatic discovery.
This can be helpful to allow replacing bean classes with a different implementation, typically a mock or stub.

This annotation works as an inverse of [`@AddBeanClasses`](#addbeanclasses) hence usually requires actual bean
implementation classes as parameters.

NOTE: This annotation will only exclude beans defined by class annotations.
It will not exclude beans of the specified type that are defined by `Produces` producer methods / fields or
synthetic beans.

## Additional Configuration

This section describes any additional configuration options this extension offers.

### Explicit Parameter Injection

As mentioned above, Weld is greedy when it comes to parameter injection.
It will claim the ability to resolve any parameter which is known as a bean type inside the running CDI container,
if it is not a data variable and not already provided by another Spock extension when this one does its work.
This is mainly for usability, as it would be annoying to constantly type additional annotations to mark
which parameter should be injected and which should be left alone.

However, we are aware that this might cause trouble if more extensions are competing for parameter resolution.
In such case, you can turn on explicit parameter resolution and Weld will only resolve parameters
which have at least one `@Qualifier` annotation on them.

The explicit parameter injection can be enabled using the `explicitParamInjection` property in the Spock configuration
file or using the same named property of the `@EnableWeld` annotation.

_**Example:**_
```groovy
// all methods will now require explicit parameters
@EnableWeld(explicitParamInjection = true)
class ExplicitParamInjectionTest extends Specification {
    def 'params should not be resolved by Weld'(Foo foo) {
        // Weld will not attempt to resolve Foo,
        // hence this test will fail unless there is another extension resolving it
    }

    def 'params should be resolved by Weld'(@Default Foo foo, @MyQualifier Bar bar) {
        // Weld will resolve both of the parameters
    }
}
```

If you want to inject a bean where you would normally not use any qualifier,
you can do that using `@Default` qualifier (as shown in the example above).
This is in accordance with the CDI specification, feel free to
[read more about it](https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#builtin_qualifiers).

#### Flat Deployment

Unlike [Arquillian Weld embedded container](https://github.com/arquillian/arquillian-container-weld),
weld-spock has bean archive isolation enabled by default. This behaviour can be changed by setting the system property
`org.jboss.weld.se.archive.isolation` to `false` or through the `Weld.property()` method using the same property.
If set to `false`, Weld will use a _"flat"_ deployment structure - all bean classes share the same bean archive
and all `beans.xml` descriptors are automatically merged into one. Thus, alternatives, interceptors,
and decorators selected / enabled for a bean archive will be enabled for the whole application.

Note that this configuration only makes a difference if you run with *enabled discovery*;
it won't affect your deployment if you use the synthetic bean archive.

### IllegalStateException in Assertion Failure Rendering

Spock has a very handy rendering capability for failed assertions called power assertions.
This rendering shows a textual representation of all sub expressions of an assertion,
most often this is a simple `toString()` result.

These power assertion renderings in Spock happen only when necessary, that is when the output is to be displayed.
If some of the sub expressions are CDI proxies, this can become problematic, as the Weld container is long shut down
already at the time the `toString()` method is called. This usually manifests with some of the sub expressions being
rendered like `Something$Proxy$_$$_WeldClientProxy@4303056f (renderer threw IllegalStateException)`.

_**Example:**_
```
Condition not satisfied:

first.id != second.id
|     |  |  |      |
|     |  |  |      1239bf1f-448c-4476-9202-f98431027196
|     |  |  org.jboss.weld.spock.bean.AddBeanTest$IdSupplier$Proxy$_$$_WeldClientProxy@7ddfaeca (renderer threw IllegalStateException)
|     |  false
|     1239bf1f-448c-4476-9202-f98431027196
org.jboss.weld.spock.bean.AddBeanTest$IdSupplier$Proxy$_$$_WeldClientProxy@7ddfaeca (renderer threw IllegalStateException)
```

This project contains another Spock extension that automatically triggers these renderings while the Weld container
is still running, as the result is cached by Spock. So with extension doing its work properly, the same example as
above renders like:

_**Example:**_
```
Condition not satisfied:

first.id != second.id
|     |  |  |      |
|     |  |  |      e6b47bae-a5d9-48c6-9d8e-bdffe4a00779
|     |  |  org.jboss.weld.spock.bean.AddBeanTest$IdSupplier@7853386b
|     |  false
|     e6b47bae-a5d9-48c6-9d8e-bdffe4a00779
org.jboss.weld.spock.bean.AddBeanTest$IdSupplier@7853386b
```

If you still see such an `IllegalStateException` in a renderer, the extension is missing some case,
so please report it as issue, optimally with a reproducer.
