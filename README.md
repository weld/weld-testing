# Weld Testing Extensions

[![Maven Central](https://img.shields.io/maven-central/v/org.jboss.weld/weld-junit4.svg)](https://central.sonatype.com/artifact/org.jboss.weld/weld-junit4)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The primary goal of this project is to provide simple and fast tools for CDI *unit/component* testing.
The tools are implemented as JUnit 4, JUnit Jupiter and Spock extensions.

:warning: **Version 6.x of this extension supports Weld 7.0 (CDI 5.0). The JUnit Jupiter extension artifact has been renamed from `weld-junit5` to `weld-junit-jupiter` — see the [migration guide](junit-jupiter/README.md#migrating-from-weld-junit5) for details.**

Java 17 or newer is required.

## The What

Extensions in this repository allow you to write unit tests for your beans regardless of the target environment (Java SE, Jakarta EE, etc.).
They start a real CDI container with minimal configuration meaning you can leverage all bean capabilities - injection, interception, events, etc. - without the need for mocking.
These extensions boot up CDI container before each test container run and shut it down afterwards.
You have the power to customize what beans, extensions, interceptors (and so on) are going to be in the container.
Furthermore, you can `@Inject` directly in your test class - and the list goes on...


## The Why

As a matter of fact, one could have a unit test for CDI bean without running a container, but that would present quite a few drawbacks.
Simulating field injection to start with, then interceptors and/or decorators - all in all, quite a challenge.
There are frameworks to make this easier such as [Mockito](https://site.mockito.org/); but use too many mocks and things get tangled real quick.
So we came with JUnit/Spock extensions which allow you to use actual CDI container instead of complex simulations.
There is no need to change the way you develop your CDI components if you have a real container to test it with.
Besides, it's easy to combine this approach with mocking frameworks (see for instance [Adding mock beans](junit-jupiter/README.md#adding-mock-beans)).

## How To Use Each Extension

This project consists of four modules.
Below is a list with links to detailed README of each extension:

* JUnit 4 extension
  * [JUnit 4 extension using `@Rule` and `@ClassRule` mechanism](junit4/README.md)
* JUnit Jupiter
  * [JUnit Jupiter extension using the `@ExtendWith` mechanism](junit-jupiter/README.md)
* JUnit-common
  * Houses the code shared by the JUnit and Spock extensions
* Spock
  * [Spock framework extension](spock/README.md)
